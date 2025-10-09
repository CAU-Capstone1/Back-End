package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.response.JobStatusResponse;
import com.capstone.HummingService.service.JobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/status")
    public JobStatusResponse getStatus(@RequestParam("jobId") String publicJobId) {
        return jobService.getJobStatus(publicJobId);
    }
}
