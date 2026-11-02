package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaleJobRecoveryTest {

    @Mock
    private JobRepository jobRepository;
    @Mock
    private ComposeProcessRepository composeProcessRepository;

    @InjectMocks
    private StaleJobRecovery staleJobRecovery;

    @Test
    void 서버_시작_시_미완료_Job을_FAILED로_정리한다() {
        Job job = new Job();
        job.setProcessId(2L);
        job.setStatus(JobStatus.PROCESSING);
        ComposeProcess process = new ComposeProcess();
        process.setStatus(ProcessStatus.PROCESSING);

        when(jobRepository.findByStatusIn(List.of(JobStatus.QUEUED, JobStatus.PROCESSING))).thenReturn(List.of(job));
        when(composeProcessRepository.findById(2L)).thenReturn(Optional.of(process));

        staleJobRecovery.failInterruptedJobs();

        assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(job.getErrorMessage()).isEqualTo(StaleJobRecovery.INTERRUPTED_MESSAGE);
        assertThat(process.getStatus()).isEqualTo(ProcessStatus.FAILED);
    }

    @Test
    void 미완료_Job이_없으면_아무것도_변경하지_않는다() {
        when(jobRepository.findByStatusIn(any())).thenReturn(List.of());

        staleJobRecovery.failInterruptedJobs();

        verify(jobRepository, never()).save(any());
    }
}
