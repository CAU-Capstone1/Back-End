package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.dto.request.CallbackRequest;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.capstone.HummingService.domain.enums.ProcessStatus;


@Service
public class CallbackService {

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;

    public CallbackService(JobRepository jobRepository, ComposeProcessRepository composeProcessRepository) {
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
    }


    //AI 서버 → 백엔드로 오는 “작업 진행/완료/실패” 콜백 처리

    @Transactional
    public void handleCallback(CallbackRequest req) {
        // 1) 콜백 대상 Job 조회
        Job job = jobRepository.findByPublicJobId(req.jobId())
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + req.jobId()));

        // 2) Job이 속한 ComposeProcess 조회 (Job.processId 사용)
        ComposeProcess process = composeProcessRepository.findById(job.getProcessId())
                .orElseThrow(() -> new IllegalStateException("ComposeProcess not found: " + job.getProcessId()));

        // 3) 상태 분기
        JobStatus status = req.status();
        if (status == JobStatus.PROCESSING) {
            job.setStatus(JobStatus.PROCESSING);
            // 진행률이 콜백에 포함되어 있다면 반영 (없으면 무시)
            if (req.progress() != null) {
                double p = Math.max(0.0, Math.min(100.0, req.progress()));
                job.setProgress(p);
            }
            process.setStatus(ProcessStatus.PROCESSING);
            process.setLastJobId(job.getPublicJobId());
            return; // 중간 진행 콜백이면 여기서 종료
        }

        if (status == JobStatus.SUCCEEDED) {
            job.setStatus(JobStatus.SUCCEEDED);
            job.setProgress(100.0);

            // 결과 오디오가 숫자 ID로 온 경우(권장) → Job/Process에 반영
            if (req.audioId() != null) {
                job.setOutputAudioId(req.audioId());
                process.setAudioId(req.audioId());
            }

            if (req.audioUrl() != null) {
            }

            process.setStatus(ProcessStatus.DONE);
            process.setLastJobId(job.getPublicJobId());
            return;
        }

        if (status == JobStatus.FAILED) {
            job.setStatus(JobStatus.FAILED);
            // 진행률 0~99에서 실패할 수 있으므로 현재 값 유지하거나 0으로
            if (req.progress() != null) {
                double p = Math.max(0.0, Math.min(100.0, req.progress()));
                job.setProgress(p);
            }
            if (req.message() != null) {
                job.setErrorMessage(req.message());
            }
            process.setStatus(ProcessStatus.FAILED);
            process.setLastJobId(job.getPublicJobId());
        }
    }
}


