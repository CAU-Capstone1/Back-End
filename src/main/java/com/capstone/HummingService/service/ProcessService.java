package com.capstone.HummingService.service;

import com.capstone.HummingService.domain.ComposeProcess;
import com.capstone.HummingService.domain.Job;
import com.capstone.HummingService.domain.enums.JobStatus;
import com.capstone.HummingService.domain.enums.ProcessStatus;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.repository.ComposeProcessRepository;
import com.capstone.HummingService.repository.JobRepository;
import com.capstone.HummingService.support.IdGenerator;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessService.class);

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;
    private final IdGenerator idGenerator;
    private final AudioService audioService;
    private final ComposeWorker composeWorker;

    // 생성자 주입
    public ProcessService(JobRepository jobRepository,
                          ComposeProcessRepository composeProcessRepository,
                          IdGenerator idGenerator,
                          AudioService audioService,
                          ComposeWorker composeWorker) {
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
        this.idGenerator = idGenerator;
        this.audioService = audioService;
        this.composeWorker = composeWorker;
    }

    // Job만 생성하고 바로 반환한다. 실제 생성은 ComposeWorker가 백그라운드에서 수행하며,
    // 클라이언트는 /api/job/{jobId}를 폴링해 진행 상태와 결과 URL을 확인한다.
    // AI 호출을 트랜잭션 밖으로 빼기 위해 이 메서드에는 @Transactional을 걸지 않는다.
    public ComposeResponse composeWithSongForm(ComposeRequest request, Long userId) {

        // 1. 파일 경로 처리 및 검증 (본인이 업로드한 파일만 사용 가능)
        List<String> hummingPaths = Stream.of(
                        request.getHummingStart(),
                        request.getHummingMain(),
                        request.getHummingEnd()
                )
                .filter(path -> path != null && !path.trim().isEmpty())
                .collect(Collectors.toList());

        if (hummingPaths.isEmpty()) {
            throw new IllegalArgumentException("허밍 파일을 1개 이상 업로드해 주세요.");
        }

        String imagePath = request.getReferenceVisual();
        boolean hasImage = imagePath != null && !imagePath.trim().isEmpty();

        if (hummingPaths.stream().anyMatch(path -> !audioService.isUploadedBy(path, userId))
                || (hasImage && !audioService.isUploadedBy(imagePath, userId))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인이 업로드한 파일만 사용할 수 있습니다.");
        }

        Integer durationSec;
        try {
            durationSec = Integer.parseInt(request.getDuration());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("길이(duration) 값이 숫자가 아닙니다: " + request.getDuration(), e);
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
        composeProcess.setInstruments(request.getInstrument());
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

        // 4. 백그라운드 작업 등록 (대기열이 가득 차면 실패 처리 후 503 응답)
        try {
            composeWorker.compose(job.getId(), composeProcess.getId(), request, durationSec);
        } catch (TaskRejectedException e) {
            logger.warn("작곡 대기열이 가득 차 요청을 거절합니다. JobId={}", job.getPublicJobId());

            job.setStatus(JobStatus.FAILED);
            job.setErrorMessage("요청이 많아 잠시 후 다시 시도해 주세요.");
            composeProcess.setStatus(ProcessStatus.FAILED);
            jobRepository.save(job);
            composeProcessRepository.save(composeProcess);

            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "요청이 많아 잠시 후 다시 시도해 주세요.");
        }

        // 5. 클라이언트에 작업 ID 반환 (상태: QUEUED)
        return new ComposeResponse(job.getPublicJobId(), job.getStatus());
    }
}
