package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.dto.response.JobStatusResponse;
import com.capstone.HummingService.repository.AudioRepository;
import com.capstone.HummingService.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit; // ⬅️ TimeUnit import


@Service
public class JobService {

    private final JobRepository jobRepository;
    private final AudioRepository audioRepository;
    private final S3Service s3Service;

    // ⭐️ 생성자 주입
    public JobService(JobRepository jobRepository, AudioRepository audioRepository, S3Service s3Service) {
        this.jobRepository = jobRepository;
        this.audioRepository = audioRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public JobStatusResponse getJobStatus(String publicJobId) {
        Optional<Job> optionalJob = jobRepository.findByPublicJobId(publicJobId);
        Job job = optionalJob.orElseThrow(() ->
                new IllegalArgumentException("해당 Job ID를 찾을 수 없습니다: " + publicJobId)
        );

        String musicUrl = null;

        if (job.getStatus() == JobStatus.SUCCEEDED && job.getOutputAudioId() != null) {

            Optional<Audio> optionalAudio = audioRepository.findById(job.getOutputAudioId());

            if (optionalAudio.isPresent()) {
                Audio finalAudio = optionalAudio.get();
                String s3Key = finalAudio.getFilePath();

                // 🌟 [수정] 유효 기간을 5분(TimeUnit.MINUTES)에서 7일(TimeUnit.DAYS)로 변경 🌟
                // 7일은 AWS S3 Pre-signed URL의 최대 유효 기간입니다.
                musicUrl = s3Service.generatePresignedUrl(s3Key, 7, TimeUnit.DAYS); // <--- 이 부분을 수정합니다.
            }
        }

        return new JobStatusResponse(
                job.getPublicJobId(),
                job.getStatus(),
                job.getProgress(),
                job.getErrorMessage(),
                musicUrl // ⬅️ 이 필드에 S3 URL이 담겨 프론트엔드로 전달됩니다.
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



