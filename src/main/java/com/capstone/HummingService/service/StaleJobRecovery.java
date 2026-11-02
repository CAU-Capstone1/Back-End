package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

// 작곡 대기열은 메모리에만 존재하므로, 서버가 비정상 종료되면 QUEUED/PROCESSING Job이 영원히 끝나지 않는다.
// 서버 시작 시 이런 Job을 FAILED로 정리해 클라이언트 폴링이 종료되도록 한다. (단일 서버 배포 기준)
@Component
public class StaleJobRecovery {

    private static final Logger logger = LoggerFactory.getLogger(StaleJobRecovery.class);
    static final String INTERRUPTED_MESSAGE = "서버 재시작으로 작업이 중단되었습니다. 다시 요청해 주세요.";

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;

    public StaleJobRecovery(JobRepository jobRepository, ComposeProcessRepository composeProcessRepository) {
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void failInterruptedJobs() {
        List<Job> interrupted = jobRepository.findByStatusIn(List.of(JobStatus.QUEUED, JobStatus.PROCESSING));
        if (interrupted.isEmpty()) {
            return;
        }

        for (Job job : interrupted) {
            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(INTERRUPTED_MESSAGE);
            jobRepository.save(job);

            composeProcessRepository.findById(job.getProcessId()).ifPresent(process -> {
                process.setStatus(ProcessStatus.FAILED);
                composeProcessRepository.save(process);
            });
        }

        logger.warn("서버 재시작으로 중단된 작업 {}건을 FAILED 처리했습니다.", interrupted.size());
    }
}
