package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.response.JobStatusResponse;
import com.capstone.HummingService.service.CurrentUserService;
import com.capstone.HummingService.service.JobService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/job")
public class JobController {

    private final JobService jobService;
    private final CurrentUserService currentUserService;

    public JobController(JobService jobService, CurrentUserService currentUserService) {
        this.jobService = jobService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobStatusResponse> getJobStatus(@PathVariable("jobId") String publicJobId,
                                                          @AuthenticationPrincipal UserDetails principal) {
        Long userId = currentUserService.getUserId(principal);
        JobStatusResponse statusResponse = jobService.getJobStatus(publicJobId, userId);

        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache") // HTTP 1.0 호환성
                .header("Expires", "0") // 프록시 캐시 방지 (과거 방식)
                .body(statusResponse);
    }
}
