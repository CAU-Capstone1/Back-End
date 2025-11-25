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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProcessService {

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;
    private final AiClientPort aiClient;
    private final IdGenerator idGenerator;
    private final AudioService audioService; // 파일 저장 기능은 이제 사용되지 않음 (프론트엔드에서 선행 처리)

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

    @Transactional
    public ComposeResponse composeWithSongForm(ComposeRequest request, Long userId) {

        // 1. 파일 경로 처리 (프론트엔드에서 이미 업로드된 String 경로를 DTO에서 가져옴)

        // 3개의 허밍 경로를 필터링하여 리스트로 통합
        List<String> hummingPaths = Arrays.asList(
                        request.getHummingStart(),
                        request.getHummingMain(),
                        request.getHummingEnd()
                ).stream()
                .filter(path -> path != null && !path.trim().isEmpty()) // null 또는 빈 문자열 제외
                .collect(Collectors.toList());

        // 이미지 경로는 DTO에서 직접 가져옴
        String imagePath = request.getReferenceVisual();

        // duration 필드는 String으로 오므로 Integer로 변환 준비
        Integer durationSec;
        try {
            durationSec = Integer.parseInt(request.getDuration());
        } catch (NumberFormatException e) {
            // 400 에러를 유발하는 대신, 런타임 예외를 발생시켜 서버 로그에 기록
            throw new RuntimeException("길이(duration) 값이 숫자가 아닙니다: " + request.getDuration(), e);
        }


        // 2. ComposeProcess 생성 및 모든 데이터 매핑 (DB 저장을 위한 엔티티)
        ComposeProcess composeProcess = new ComposeProcess();
        composeProcess.setAudioId(null);

        // 사용자 ID 연결
        composeProcess.setUserId(userId);

        // 허밍 경로 저장 (콤마로 연결)
        composeProcess.setHummingPath(String.join(",", hummingPaths));
        // 시각 자료 경로 저장
        composeProcess.setImagePath(imagePath);

        // 음악 설정 (프론트 DTO 필드명에 맞추어 매핑)
        composeProcess.setStyle(request.getStyle()); // DTO: style -> DB: genre
        composeProcess.setMood(request.getMood());
        composeProcess.setDurationSec(durationSec); // 변환된 Integer 값 저장
        composeProcess.setKeyInfo(request.getKey());
        composeProcess.setTempoInfo(request.getTempo());

        // Instrument 처리 (단일 String으로 저장)
        if (request.getInstrument() != null) {
            composeProcess.setInstruments(request.getInstrument());
        } else {
            composeProcess.setInstruments(null);
        }

        composeProcess.setStatus(ProcessStatus.CREATED);
        composeProcessRepository.save(composeProcess);

        // 3. Job 생성 (AI 작업 요청 건)
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

        /*
        // 4. AI Client Port를 통해 AI 작곡 모듈에 요청 전송
        aiClient.composeWithSongForm(
                job.getPublicJobId(),
                // 3개의 허밍 경로를 개별 인자로 전달
                request.getHummingStart(),
                request.getHummingMain(),
                request.getHummingEnd(),

                imagePath, // referenceVisual 경로
                request.getStyle(), // style
                durationSec, // Integer 값 전달
                request.getKey(),
                request.getTempo(),
                request.getInstrument(), // 단일 String
                "" // prompt 인자
        );*/

        // 5. 클라이언트에 작업 상태 응답 반환
        return new ComposeResponse(job.getPublicJobId(), job.getStatus());
    }
}