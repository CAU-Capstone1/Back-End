package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.enums.JobStatus;

public record JobStatusResponse(String jobId, JobStatus status, double progress, String errorMessage) {
}

// 클라이언트가 특정 Job의 진행 상태를 조회할 때 받음