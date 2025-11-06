package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.port.ai.AiClientPort;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import com.capstone.HummingService.support.IdGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.stream.Collectors; // List 처리용

@Service
public class ProcessService {

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;
    private final AiClientPort aiClient;
    private final IdGenerator idGenerator;
    private final AudioService audioService;

    // 생성자 주입
    public ProcessService(JobRepository jobRepository,
                          ComposeProcessRepository composeProcessRepository,
                          AiClientPort aiClient,
                          IdGenerator idGenerator,
                          AudioService audioService) {
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
        this.aiClient = aiClient;
        this.idGenerator = idGenerator;
        this.audioService = audioService;
    }

    /**
     * ✅ 새로운 주력 메소드: 파일 업로드 및 6가지 음악적 요소 처리
     * 클라이언트로부터 받은 파일과 메타데이터를 처리하고 AI 모듈에 작곡을 요청합니다.
     */
    @Transactional
    public ComposeResponse composeFull(
            MultipartFile hummingFile,
            MultipartFile imageFile,
            ComposeRequest composeRequest
    ) {
        String hummingPath;
        String imagePath = null;

        try {
            // 1. S3 (또는 임시 로컬 저장소)에 파일 저장 및 경로 획득
            hummingPath = audioService.saveAudio(hummingFile);

            if (imageFile != null && !imageFile.isEmpty()) {
                imagePath = audioService.saveImage(imageFile);
            }
        } catch (IOException e) {
            // 파일 업로드 실패 시 예외 처리
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        // 2. ComposeProcess 생성 및 모든 데이터 매핑
        ComposeProcess composeProcess = new ComposeProcess();
        composeProcess.setAudioId(null);

        // 파일 경로 저장
        composeProcess.setHummingPath(hummingPath);
        composeProcess.setImagePath(imagePath);

        // 6가지 음악적 요소 저장
        composeProcess.setGenre(composeRequest.genre());
        composeProcess.setDurationSec(composeRequest.durationSec());
        composeProcess.setKeyInfo(composeRequest.key());
        composeProcess.setTempoInfo(composeRequest.tempo());

        // List<String>인 instruments를 쉼표 구분 문자열로 변환하여 DB에 저장
        if (composeRequest.instruments() != null) {
            composeProcess.setInstruments(String.join(",", composeRequest.instruments()));
        }

        // 텍스트 프롬프트 저장
        composeProcess.setPrompt(composeRequest.imagePrompt());

        composeProcess.setStatus(ProcessStatus.CREATED);
        composeProcessRepository.save(composeProcess); // Process를 DB에 저장

        // 3. Job 생성 (AI 작업 요청 건)
        Job job = new Job();
        job.setPublicJobId(idGenerator.generate("job"));
        job.setProcessId(composeProcess.getId());
        job.setStatus(JobStatus.QUEUED);
        job.setProgress(0.0);
        jobRepository.save(job);

        // Process에 최종 Job ID 연결 및 저장
        composeProcess.setLastJobId(job.getPublicJobId());
        composeProcessRepository.save(composeProcess);

        // 4. AI Client Port를 통해 AI 작곡 모듈에 요청 전송
        aiClient.compose(
                job.getPublicJobId(),
                hummingPath,  // S3 Key 전달
                imagePath,    // 이미지 S3 Key 전달 (null 가능)
                composeRequest.genre(),
                composeRequest.durationSec(),
                composeRequest.key(),
                composeRequest.tempo(),
                composeRequest.instruments(), // List<String> 형태로 전달 (AiClientPort에서 처리)
                composeRequest.imagePrompt()
        );

        // 5. 클라이언트에 작업 상태 응답 반환
        return new ComposeResponse(job.getPublicJobId(), job.getStatus());
    }
}