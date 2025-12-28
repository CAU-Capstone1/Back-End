package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.enums.JobStatus;

public record ComposeResponse(String PublicJobId, JobStatus status) {
}

