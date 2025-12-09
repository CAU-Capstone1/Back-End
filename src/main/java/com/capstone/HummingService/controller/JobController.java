package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.response.JobStatusResponse;
import com.capstone.HummingService.service.JobService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity; // 이미 임포트되어 있음

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/{jobId}")    // 💡 1. 반환 타입을 ResponseEntity<JobStatusResponse>로 변경합니다.
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable("jobId") String publicJobId) {
        // 2. JobService에서 상태 정보를 가져옵니다.
        JobStatusResponse statusResponse = jobService.getJobStatus(publicJobId);

        // 💡 3. ResponseEntity.ok()를 사용하여 200 OK 응답을 만들고,
        //    캐싱 방지 헤더를 추가한 후, 응답 본문에 상태 정보를 담아 반환합니다.
        return ResponseEntity.ok()
                // 클라이언트와 프록시 서버 모두 캐시를 사용하지 않도록 지시
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache") // HTTP 1.0 호환성
                .header("Expires", "0") // 프록시 캐시 방지 (과거 방식)
                .body(statusResponse);
    }
}