package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.dto.response.JobStatusResponse;
import com.capstone.HummingService.repository.AudioRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Service
public class JobService {

    private final JobRepository jobRepository;
    private final AudioRepository audioRepository;
    private final S3Service s3Service;

    public JobService(JobRepository jobRepository, AudioRepository audioRepository, S3Service s3Service) {
        this.jobRepository = jobRepository;
        this.audioRepository = audioRepository;
        this.s3Service = s3Service;
    }

    @Transactional(readOnly = true)
    public JobStatusResponse getJobStatus(String publicJobId, Long userId) {
        // 다른 사용자의 Job은 존재 여부도 드러나지 않도록 404로 응답
        Job job = jobRepository.findByPublicJobId(publicJobId)
                .filter(j -> j.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 Job ID를 찾을 수 없습니다: " + publicJobId));

        String musicUrl = null;

        if (job.getStatus() == JobStatus.SUCCEEDED && job.getOutputAudioId() != null) {

            Optional<Audio> optionalAudio = audioRepository.findById(job.getOutputAudioId());

            if (optionalAudio.isPresent()) {
                Audio finalAudio = optionalAudio.get();
                String s3Key = finalAudio.getFilePath();


                musicUrl = s3Service.generatePresignedUrl(s3Key, 7, TimeUnit.DAYS);
            }
        }

        return new JobStatusResponse(
                job.getPublicJobId(),
                job.getStatus(),
                job.getProgress(),
                job.getErrorMessage(),
                musicUrl
        );
    }
}



