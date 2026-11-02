package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.CallbackRequest;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallbackServiceTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private ComposeProcessRepository composeProcessRepository;

    @InjectMocks
    private CallbackService callbackService;

    @Test
    void 진행_콜백은_진행률을_0에서_100_사이로_반영한다() {
        Job job = givenJob(JobStatus.QUEUED);

        callbackService.handleCallback(callback(JobStatus.PROCESSING, 150.0));

        assertThat(job.getStatus()).isEqualTo(JobStatus.PROCESSING);
        assertThat(job.getProgress()).isEqualTo(100.0);
    }

    @Test
    void 이미_완료된_Job은_늦게_도착한_콜백으로_상태가_바뀌지_않는다() {
        Job job = givenJob(JobStatus.SUCCEEDED);

        callbackService.handleCallback(callback(JobStatus.FAILED, 10.0));

        assertThat(job.getStatus()).isEqualTo(JobStatus.SUCCEEDED);
    }

    private Job givenJob(JobStatus status) {
        Job job = new Job();
        job.setPublicJobId("job_abc");
        job.setProcessId(2L);
        job.setStatus(status);

        ComposeProcess process = new ComposeProcess();
        process.setStatus(ProcessStatus.PROCESSING);

        when(jobRepository.findByPublicJobId("job_abc")).thenReturn(Optional.of(job));
        when(composeProcessRepository.findById(2L)).thenReturn(Optional.of(process));
        return job;
    }

    private CallbackRequest callback(JobStatus status, Double progress) {
        return new CallbackRequest("job_abc", status, progress, null, null, null, null);
    }
}
