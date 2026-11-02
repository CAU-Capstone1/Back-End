package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import com.capstone.HummingService.support.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private ComposeProcessRepository composeProcessRepository;
    @Mock
    private IdGenerator idGenerator;
    @Mock
    private AudioService audioService;
    @Mock
    private ComposeWorker composeWorker;

    @InjectMocks
    private ProcessService processService;

    @Test
    void 요청은_AI_호출을_기다리지_않고_QUEUED로_즉시_반환된다() {
        ComposeRequest request = request("humming/1/a.mp3");
        when(audioService.isUploadedBy("humming/1/a.mp3", 1L)).thenReturn(true);
        when(idGenerator.generate("job")).thenReturn("job_abc");

        ComposeResponse res = processService.composeWithSongForm(request, 1L);

        assertThat(res.jobId()).isEqualTo("job_abc");
        assertThat(res.status()).isEqualTo(JobStatus.QUEUED);
        verify(composeWorker).compose(any(), any(), eq(request), eq(30));
    }

    @Test
    void 키_템포_길이를_건너뛰면_기본_길이로_요청된다() {
        ComposeRequest request = request("humming/1/a.mp3");
        request.setKey(null);
        request.setTempo(null);
        request.setDuration(null);
        when(audioService.isUploadedBy("humming/1/a.mp3", 1L)).thenReturn(true);
        when(idGenerator.generate("job")).thenReturn("job_abc");

        ComposeResponse res = processService.composeWithSongForm(request, 1L);

        assertThat(res.status()).isEqualTo(JobStatus.QUEUED);
        verify(composeWorker).compose(any(), any(), eq(request), eq(ProcessService.DEFAULT_DURATION_SEC));
    }

    @Test
    void 허밍_파일이_없으면_Job을_만들지_않는다() {
        assertThatThrownBy(() -> processService.composeWithSongForm(request(null), 1L))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(jobRepository, composeWorker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "31", "100000", "abc"})
    void 허용_범위를_벗어난_길이는_거부한다(String duration) {
        ComposeRequest request = request("humming/1/a.mp3");
        request.setDuration(duration);
        when(audioService.isUploadedBy("humming/1/a.mp3", 1L)).thenReturn(true);

        assertThatThrownBy(() -> processService.composeWithSongForm(request, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(jobRepository, composeWorker);
    }

    @Test
    void 다른_사용자의_파일로는_작곡을_요청할_수_없다() {
        when(audioService.isUploadedBy("humming/2/a.mp3", 1L)).thenReturn(false);

        assertThatThrownBy(() -> processService.composeWithSongForm(request("humming/2/a.mp3"), 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(jobRepository, composeWorker);
    }

    @Test
    void 진행_중인_작업이_한도에_도달하면_429를_반환한다() {
        when(audioService.isUploadedBy("humming/1/a.mp3", 1L)).thenReturn(true);
        when(jobRepository.countByUserIdAndStatusIn(eq(1L), any())).thenReturn((long) ProcessService.MAX_ACTIVE_JOBS_PER_USER);

        assertThatThrownBy(() -> processService.composeWithSongForm(request("humming/1/a.mp3"), 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        verify(jobRepository, never()).save(any());
        verifyNoInteractions(composeWorker);
    }

    @Test
    void 대기열이_가득_차면_Job을_FAILED로_기록하고_503을_반환한다() {
        when(audioService.isUploadedBy(anyString(), eq(1L))).thenReturn(true);
        when(idGenerator.generate("job")).thenReturn("job_abc");
        doThrow(new TaskRejectedException("full")).when(composeWorker).compose(any(), any(), any(), any());

        assertThatThrownBy(() -> processService.composeWithSongForm(request("humming/1/a.mp3"), 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);

        ArgumentCaptor<Job> job = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository, atLeastOnce()).save(job.capture());
        assertThat(job.getValue().getStatus()).isEqualTo(JobStatus.FAILED);

        ArgumentCaptor<ComposeProcess> process = ArgumentCaptor.forClass(ComposeProcess.class);
        verify(composeProcessRepository, atLeastOnce()).save(process.capture());
        assertThat(process.getValue().getStatus()).isEqualTo(ProcessStatus.FAILED);
    }

    private ComposeRequest request(String hummingMain) {
        ComposeRequest request = new ComposeRequest();
        request.setHummingMain(hummingMain);
        request.setKey("C");
        request.setTempo("120");
        request.setDuration("30");
        request.setStyle("pop");
        request.setMood("happy");
        return request;
    }
}
