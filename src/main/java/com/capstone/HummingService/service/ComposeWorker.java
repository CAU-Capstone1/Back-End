package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.AudioType;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.port.ai.AiClientPort;
import com.capstone.HummingService.repository.AudioRepository;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// AI 작곡 요청을 백그라운드에서 실행한다.
// AI 호출(수십 초)은 트랜잭션 밖에서 수행하고, 상태 변경은 각 단계마다 바로 저장해 실패 기록이 롤백되지 않도록 한다.
@Component
public class ComposeWorker {

    private static final Logger logger = LoggerFactory.getLogger(ComposeWorker.class);

    static final double PROGRESS_STARTED = 10.0;   // AI 서버 요청 시작
    static final double PROGRESS_GENERATED = 80.0; // 음원 생성 완료, 저장 중

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;
    private final AudioRepository audioRepository;
    private final AiClientPort aiClient;
    private final S3Service s3Service;

    public ComposeWorker(JobRepository jobRepository,
                         ComposeProcessRepository composeProcessRepository,
                         AudioRepository audioRepository,
                         AiClientPort aiClient,
                         S3Service s3Service) {
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
        this.audioRepository = audioRepository;
        this.aiClient = aiClient;
        this.s3Service = s3Service;
    }

    @Async("composeExecutor")
    public void compose(Long jobId, Long processId, ComposeRequest request, Integer durationSec) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalStateException("Job not found: " + jobId));
        ComposeProcess composeProcess = composeProcessRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("ComposeProcess not found: " + processId));

        // 진행률은 AI 서버가 중간 진행 상황을 알려주지 않으므로 처리 단계 기준으로 기록한다
        // 1. 실행 시작 상태 기록
        job.setStatus(JobStatus.PROCESSING);
        job.setProgress(PROGRESS_STARTED);
        composeProcess.setStatus(ProcessStatus.PROCESSING);
        jobRepository.save(job);
        composeProcessRepository.save(composeProcess);

        logger.info("AI 요청 시작: JobId={}", job.getPublicJobId());

        try {
            // 2. AI 서버에 요청을 보내고, 생성된 BGM 파일의 byte[] 데이터를 응답으로 받음
            byte[] bgmFileData = aiClient.composeWithSongForm(
                    job.getPublicJobId(),
                    request.getHummingStart(),
                    request.getHummingMain(),
                    request.getHummingEnd(),
                    request.getReferenceVisual(),
                    request.getStyle(),
                    request.getMood(),
                    durationSec,
                    request.getKey(),
                    request.getTempo(),
                    request.getInstrument(),
                    "" // prompt 인자
            );

            job.setProgress(PROGRESS_GENERATED);
            jobRepository.save(job);

            // 3. BGM 파일 데이터를 S3에 업로드
            String s3FileName = job.getPublicJobId() + ".wav"; // AI 서버 응답 타입 가정
            String finalBgmS3Key = s3Service.uploadBytes(bgmFileData, "final-bgm/", s3FileName, "audio/wav");

            logger.info("S3 업로드 완료: Key={}", finalBgmS3Key);

            // 4. Audio 엔티티를 생성하고 S3 Key 및 메타데이터를 저장
            Audio finalAudio = new Audio();
            finalAudio.setFilePath(finalBgmS3Key);
            finalAudio.setFileSize((long) bgmFileData.length);
            finalAudio.setName(s3FileName);
            finalAudio.setAudioType(AudioType.GENERATED);
            finalAudio.setGenerTime(LocalDateTime.now());
            Long finalAudioId = audioRepository.save(finalAudio).getId();

            // 5. Job 및 ComposeProcess 완료 처리
            job.setStatus(JobStatus.SUCCEEDED);
            job.setProgress(100.0);
            job.setErrorMessage(null);
            job.setOutputAudioId(finalAudioId);
            composeProcess.setStatus(ProcessStatus.DONE);
            composeProcess.setAudioId(finalAudioId);

        } catch (Exception e) {
            logger.error("BGM 생성 및 저장 중 오류 발생 JobId={}: {}", job.getPublicJobId(), e.getMessage(), e);

            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage(e.getMessage());
            composeProcess.setStatus(ProcessStatus.FAILED);
        }

        jobRepository.save(job);
        composeProcessRepository.save(composeProcess);
    }
}
