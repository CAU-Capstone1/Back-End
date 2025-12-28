package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.enums.JobStatus;

public record JobStatusResponse(
        String jobId,
        JobStatus status,
        double progress,
        String errorMessage,
        String musicUrl
) {
}