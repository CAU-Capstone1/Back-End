package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.AudioType;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.port.ai.AiClientPort;
import com.capstone.HummingService.repository.AudioRepository; // ⭐️ AudioRepository import 추가
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import com.capstone.HummingService.support.IdGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;
    private final AiClientPort aiClient;
    private final IdGenerator idGenerator;
    private final AudioService audioService;
    private final S3Service s3Service; // ⭐️ S3Service 필드 추가 ⭐️
    private final AudioRepository audioRepository; // ⭐️ AudioRepository 필드 추가 ⭐️

    // 생성자 주입
    public ProcessService(JobRepository jobRepository,
                          ComposeProcessRepository composeProcessRepository,
                          AiClientPort aiClient,
                          IdGenerator idGenerator,
                          AudioService audioService,
                          S3Service s3Service, // ⭐️ S3Service 주입 추가 ⭐️
                          AudioRepository audioRepository) { // ⭐️ AudioRepository 주입 추가 ⭐️
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
        this.aiClient = aiClient;
        this.idGenerator = idGenerator;
        this.audioService = audioService;
        this.s3Service = s3Service; // ⭐️ S3Service 할당 ⭐️
        this.audioRepository = audioRepository; // ⭐️ AudioRepository 할당 ⭐️
    }

    @Transactional
    public ComposeResponse composeWithSongForm(ComposeRequest request, Long userId) {

        // 1. 파일 경로 처리
        List<String> hummingPaths = Arrays.asList(
                        request.getHummingStart(),
                        request.getHummingMain(),
                        request.getHummingEnd()
                ).stream()
                .filter(path -> path != null && !path.trim().isEmpty())
                .collect(Collectors.toList());

        String imagePath = request.getReferenceVisual();

        Integer durationSec;
        try {
            durationSec = Integer.parseInt(request.getDuration());
        } catch (NumberFormatException e) {
            throw new RuntimeException("길이(duration) 값이 숫자가 아닙니다: " + request.getDuration(), e);
        }

        // 2. ComposeProcess 생성 및 모든 데이터 매핑
        ComposeProcess composeProcess = new ComposeProcess();
        composeProcess.setAudioId(null);
        composeProcess.setUserId(userId);
        composeProcess.setHummingPath(String.join(",", hummingPaths));
        composeProcess.setImagePath(imagePath);
        composeProcess.setStyle(request.getStyle());
        composeProcess.setMood(request.getMood());
        composeProcess.setDurationSec(durationSec);
        composeProcess.setKeyInfo(request.getKey());
        composeProcess.setTempoInfo(request.getTempo());

        if (request.getInstrument() != null) {
            composeProcess.setInstruments(request.getInstrument());
        } else {
            composeProcess.setInstruments(null);
        }

        composeProcess.setStatus(ProcessStatus.CREATED);
        composeProcessRepository.save(composeProcess);

        // 3. Job 생성
        Job job = new Job();
        job.setPublicJobId(idGenerator.generate("job"));
        job.setProcessId(composeProcess.getId());
        job.setStatus(JobStatus.QUEUED);
        job.setProgress(0.0);
        job.setUserId(userId);
        jobRepository.save(job);

        // Process에 최종 Job ID 연결 및 저장
        composeProcess.setLastJobId(job.getPublicJobId());
        composeProcessRepository.save(composeProcess);


        // 4. AI Client Port를 통해 AI 작곡 모듈에 요청 전송 및 결과 처리 (핵심 로직)
        logger.info("AI 요청 시작: JobId={}", job.getPublicJobId());

        try {
            // 4-1. AI 서버에 요청을 보내고, 생성된 BGM 파일의 byte[] 데이터를 응답으로 받음
            byte[] bgmFileData = aiClient.composeWithSongForm(
                    job.getPublicJobId(),
                    request.getHummingStart(),
                    request.getHummingMain(),
                    request.getHummingEnd(),
                    imagePath,
                    request.getStyle(),
                    request.getMood(),
                    durationSec,
                    request.getKey(),
                    request.getTempo(),
                    request.getInstrument(),
                    "" // prompt 인자
            );

            logger.info("AI 요청 전송 및 BGM 데이터 수신 성공. JobId={}", job.getPublicJobId());

            // 4-2. BGM 파일 데이터를 S3에 업로드
            String fileExtension = ".wav"; // AI 서버 응답 타입 가정
            String s3FileName = job.getPublicJobId() + fileExtension;

            String finalBgmS3Key = s3Service.uploadBytes(
                    bgmFileData,
                    "final-bgm/", // AI 결과물 저장 디렉토리
                    s3FileName,
                    "audio/wav" // ContentType
            );

            logger.info("S3 업로드 완료: Key={}", finalBgmS3Key);

            // ⭐️ 4-3. Audio 엔티티를 생성하고 S3 Key 및 메타데이터를 저장합니다. ⭐️
            Audio finalAudio = new Audio();
            finalAudio.setFilePath(finalBgmS3Key);          // S3 Key 저장
            finalAudio.setFileSize((long) bgmFileData.length); // 파일 크기
            // finalAudio.setAudioLength(...)               // 오디오 길이 설정 로직이 필요하면 추가
            finalAudio.setName(s3FileName);
            finalAudio.setAudioType(AudioType.GENERATED);
            finalAudio.setGenerTime(LocalDateTime.now());

            Audio savedAudio = audioRepository.save(finalAudio);
            Long finalAudioId = savedAudio.getId();


            // 4-4. Job 및 ComposeProcess 상태 업데이트 및 Audio ID DB 저장
            job.setStatus(JobStatus.SUCCEEDED);
            job.setProgress(100.0);
            job.setErrorMessage(null); // 성공 시 에러 메시지 초기화

            // ⭐️ Job 엔티티에 Audio ID (Long) 저장 ⭐️
            job.setOutputAudioId(finalAudioId);

            composeProcess.setStatus(ProcessStatus.DONE);

            // ⭐️ ComposeProcess 엔티티에 Audio ID (Long) 저장 ⭐️
            composeProcess.setAudioId(finalAudioId);

            // 4-5. 상태 저장
            jobRepository.save(job);
            composeProcessRepository.save(composeProcess);

        } catch (Exception e) {
            // ⭐️ AI 요청, S3 업로드 중 오류 발생 시 Job/Process 상태를 FAILED로 변경 ⭐️
            logger.error("BGM 생성 및 저장 중 최종 오류 발생 JobId={}: {}", job.getPublicJobId(), e.getMessage(), e);

            job.setStatus(JobStatus.FAILED);
            job.setProgress(0.0);
            job.setErrorMessage(e.getMessage());
            composeProcess.setStatus(ProcessStatus.FAILED);

            jobRepository.save(job);
            composeProcessRepository.save(composeProcess);

            // 클라이언트에게 오류 응답을 전달하기 위해 예외를 던집니다.
            throw new RuntimeException("BGM 생성 및 저장 실패", e);
        }

        // 5. 클라이언트에 작업 상태 응답 반환
        return new ComposeResponse(job.getPublicJobId(), job.getStatus());
    }
}