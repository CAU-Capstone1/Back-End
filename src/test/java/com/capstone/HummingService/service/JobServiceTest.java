package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.dto.response.JobStatusResponse;
import com.capstone.HummingService.repository.AudioRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private AudioRepository audioRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private JobService jobService;

    @Test
    void 다른_사용자의_Job은_404로_응답한다() {
        when(jobRepository.findByPublicJobId("job_abc")).thenReturn(Optional.of(job(2L, JobStatus.SUCCEEDED, 10L)));

        assertThatThrownBy(() -> jobService.getJobStatus("job_abc", 1L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        verifyNoInteractions(s3Service);
    }

    @Test
    void 완료된_본인_Job은_결과_URL을_함께_반환한다() {
        when(jobRepository.findByPublicJobId("job_abc")).thenReturn(Optional.of(job(1L, JobStatus.SUCCEEDED, 10L)));
        Audio audio = new Audio();
        audio.setFilePath("final-bgm/job_abc.wav");
        when(audioRepository.findById(10L)).thenReturn(Optional.of(audio));
        when(s3Service.generatePresignedUrl("final-bgm/job_abc.wav", 7, TimeUnit.DAYS)).thenReturn("https://signed");

        JobStatusResponse res = jobService.getJobStatus("job_abc", 1L);

        assertThat(res.status()).isEqualTo(JobStatus.SUCCEEDED);
        assertThat(res.musicUrl()).isEqualTo("https://signed");
    }

    @Test
    void 진행_중인_Job은_결과_URL_없이_반환한다() {
        when(jobRepository.findByPublicJobId("job_abc")).thenReturn(Optional.of(job(1L, JobStatus.PROCESSING, null)));

        JobStatusResponse res = jobService.getJobStatus("job_abc", 1L);

        assertThat(res.status()).isEqualTo(JobStatus.PROCESSING);
        assertThat(res.musicUrl()).isNull();
        verifyNoInteractions(s3Service);
    }

    private Job job(Long userId, JobStatus status, Long outputAudioId) {
        Job job = new Job();
        job.setPublicJobId("job_abc");
        job.setUserId(userId);
        job.setStatus(status);
        job.setOutputAudioId(outputAudioId);
        return job;
    }
}
