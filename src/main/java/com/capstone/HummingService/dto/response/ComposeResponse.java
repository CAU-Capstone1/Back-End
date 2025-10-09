package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.enums.JobStatus;

public record ComposeResponse(String PublicJobId, JobStatus status) {
}

// composeRequest에 대한 응답 -> 새로운 Job이 생성되었을 때 클라이언트에 전달