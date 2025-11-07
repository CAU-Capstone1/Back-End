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
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public ComposeResponse composeWithSongForm(ComposeRequest request, Long userId) {

        // 1. 다중 허밍 파일 및 이미지 파일 저장 및 경로 획득
        List<String> hummingPaths;
        String imagePath = null;

        try {
            // 1-1. 다중 허밍 파일 순서대로 저장
            hummingPaths = request.getHummingFiles().stream()
                    .map(file -> {
                        try {
                            // saveAudio는 IOException을 던지므로 RuntimeException으로 감싸서 처리
                            return audioService.saveAudio(file);
                        } catch (IOException e) {
                            throw new RuntimeException("허밍 파일 저장 중 오류가 발생했습니다.", e);
                        }
                    })
                    .collect(Collectors.toList());

            // 1-2. 이미지 파일 저장 (선택적)
            if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
                // saveImage가 던질 수 있는 IOException은 아래 catch 블록에서 처리
                imagePath = audioService.saveImage(request.getImageFile());
            }

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 중 입출력 오류가 발생했습니다.", e);
        } catch (RuntimeException e) {
            // Stream.map에서 발생한 런타임 예외를 다시 던짐
            throw e;
        }

        // 2. ComposeProcess 생성 및 모든 데이터 매핑
        ComposeProcess composeProcess = new ComposeProcess();
        composeProcess.setAudioId(null);

        // 사용자 ID 연결 (로그인 기능 반영)
        composeProcess.setUserId(userId);

        composeProcess.setHummingPath(String.join(",", hummingPaths));
        composeProcess.setImagePath(imagePath);
        composeProcess.setSongFormInfo(String.join(",", request.getSongForm()));

        // 음악 설정
        composeProcess.setGenre(request.getGenre());
        composeProcess.setDurationSec(request.getDurationSec());
        composeProcess.setKeyInfo(request.getKey());
        composeProcess.setTempoInfo(request.getTempo());

        if (request.getInstruments() != null) {
            composeProcess.setInstruments(String.join(",", request.getInstruments()));
        }

        // composeProcess.setPrompt(request.getPrompt()); // DTO에 필드 유무에 따라 사용

        composeProcess.setStatus(ProcessStatus.CREATED);
        composeProcessRepository.save(composeProcess);

        // 3. Job 생성 (AI 작업 요청 건)
        Job job = new Job();
        job.setPublicJobId(idGenerator.generate("job"));
        job.setProcessId(composeProcess.getId());
        job.setStatus(JobStatus.QUEUED);
        job.setProgress(0.0);

        // Job에도 사용자 ID 연결
        job.setUserId(userId);
        jobRepository.save(job);

        // Process에 최종 Job ID 연결 및 저장
        composeProcess.setLastJobId(job.getPublicJobId());
        composeProcessRepository.save(composeProcess);

        // 4. AI Client Port를 통해 AI 작곡 모듈에 요청 전송
        aiClient.composeWithSongForm(
                job.getPublicJobId(),
                hummingPaths,
                request.getSongForm(),
                imagePath,
                request.getGenre(),
                request.getDurationSec(),
                request.getKey(),
                request.getTempo(),
                request.getInstruments(),
                "" // prompt 인자 (DTO에 없으므로 빈 문자열 전달)
        );

        // 5. 클라이언트에 작업 상태 응답 반환
        return new ComposeResponse(job.getPublicJobId(), job.getStatus());
    }
}