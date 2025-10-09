package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.dto.response.JobStatusResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import com.capstone.HummingService.repository.JobRepository;

import java.util.Optional;


@Service
public class JobService {

    private final JobRepository jobRepository;
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // Job 조회/갱신 API용 서비스

    @Transactional
    public JobStatusResponse getJobStatus(String publicJobId) {
        Optional<Job> optionalJob = jobRepository.findByPublicJobId(publicJobId);
        Job job = optionalJob.orElseThrow(() ->
                new IllegalArgumentException("해당 Job ID를 찾을 수 없습니다: " + publicJobId)
        );

        return new JobStatusResponse(
                job.getPublicJobId(),
                job.getStatus(),
                job.getProgress(),
                job.getErrorMessage()
        );
    }

    @Transactional
    public void updateJobStatus(String publicJobId, double progress, String errorMessage) {
        Optional<Job> optionalJob = jobRepository.findByPublicJobId(publicJobId);
        Job job = optionalJob.orElseThrow(() ->
                new IllegalArgumentException("해당 Job ID를 찾을 수 없습니다: " + publicJobId)
        );

        if (progress >= 100.0) {
            job.setProgress(100.0);
        } else {
            job.setProgress(progress);
        }

        if (errorMessage != null) {
            job.setErrorMessage(errorMessage);
        }

        jobRepository.save(job);
    }
}



