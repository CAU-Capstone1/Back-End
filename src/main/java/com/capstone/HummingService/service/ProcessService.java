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




@Service
public class ProcessService {

    private final JobRepository jobRepository;
    private final ComposeProcessRepository composeProcessRepository;
    private final AiClientPort aiClient;
    private final IdGenerator idGenerator;

    public ProcessService(JobRepository jobRepository, ComposeProcessRepository composeProcessRepository, AiClientPort aiClient, IdGenerator idGenerator) {
        this.jobRepository = jobRepository;
        this.composeProcessRepository = composeProcessRepository;
        this.aiClient = aiClient;
        this.idGenerator = idGenerator;
    }

   // 프로세스 생성 → 잡 생성/큐잉 → AI 호출 → 응답 DTO 반환

   @Transactional
    public ComposeResponse compose(ComposeRequest composeRequest) {

        // ComposeProcess 생성
       ComposeProcess composeProcess = new ComposeProcess();
       composeProcess.setAudioId(null);
       composeProcess.setStyle(composeRequest.style());
       composeProcess.setMood(composeRequest.mood());
       composeProcess.setStatus(ProcessStatus.CREATED);
       composeProcessRepository.save(composeProcess);

       // Job 생성
       Job job = new Job();
       job.setPublicJobId(idGenerator.generate("job"));
       job.setProcessId(composeProcess.getId());
       job.setStatus(JobStatus.QUEUED);
       job.setProgress(0.0);
       jobRepository.save(job);

       composeProcess.setLastJobId(job.getPublicJobId());
       composeProcessRepository.save(composeProcess);

       // ai에 변환 요청
       aiClient.compose(
               job.getPublicJobId(),
               composeRequest.hummingPath(),
               composeRequest.mood(),
               composeRequest.style()
       );

       return new ComposeResponse(job.getPublicJobId(), job.getStatus());



   }




}
