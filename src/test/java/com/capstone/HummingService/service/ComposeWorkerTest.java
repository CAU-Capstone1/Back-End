package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.port.ai.AiClientPort;
import com.capstone.HummingService.repository.AudioRepository;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComposeWorkerTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private ComposeProcessRepository composeProcessRepository;
    @Mock
    private AudioRepository audioRepository;
    @Mock
    private AiClientPort aiClient;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ComposeWorker composeWorker;

    private Job job;
    private ComposeProcess process;
    private final ComposeRequest request = new ComposeRequest();

    @BeforeEach
    void setUp() {
        job = new Job();
        job.setId(1L);
        job.setPublicJobId("job_abc");
        job.setStatus(JobStatus.QUEUED);

        process = new ComposeProcess();
        process.setId(2L);
        process.setStatus(ProcessStatus.CREATED);

        request.setHummingMain("humming/1/a.mp3");

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(composeProcessRepository.findById(2L)).thenReturn(Optional.of(process));
    }

    @Test
    void 생성에_성공하면_결과를_S3와_DB에_저장하고_SUCCEEDED로_변경한다() throws Exception {
        byte[] wav = {1, 2, 3};
        when(aiClient.compose(eq("job_abc"), eq("humming/1/a.mp3"), any(), eq(30)))
                .thenReturn(wav);
        when(s3Service.uploadBytes(wav, "final-bgm/", "job_abc.wav", "audio/wav")).thenReturn("final-bgm/job_abc.wav");
        when(audioRepository.save(any(Audio.class))).thenAnswer(invocation -> {
            Audio audio = invocation.getArgument(0);
            audio.setId(10L);
            return audio;
        });

        composeWorker.compose(1L, 2L, request, 30);

        assertThat(job.getStatus()).isEqualTo(JobStatus.SUCCEEDED);
        assertThat(job.getProgress()).isEqualTo(100.0);
        assertThat(job.getOutputAudioId()).isEqualTo(10L);
        assertThat(process.getStatus()).isEqualTo(ProcessStatus.DONE);
        assertThat(process.getAudioId()).isEqualTo(10L);
    }

    @Test
    void AI_호출이_실패하면_FAILED와_에러_메시지가_기록된다() throws Exception {
        when(aiClient.compose(anyString(), any(), any(), anyInt()))
                .thenThrow(new RuntimeException("AI 서버 응답 시간 초과"));

        composeWorker.compose(1L, 2L, request, 30);

        assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(job.getProgress()).isEqualTo(ComposeWorker.PROGRESS_STARTED); // 실패 지점의 진행률 유지
        assertThat(job.getErrorMessage()).isEqualTo("AI 서버 응답 시간 초과");
        assertThat(process.getStatus()).isEqualTo(ProcessStatus.FAILED);
        verifyNoInteractions(s3Service, audioRepository);
    }
}
