package com.capstone.HummingService.dto.request;

import com.capstone.HummingService.domain.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CallbackRequest(@NotBlank String jobId, @NotNull JobStatus status, Double progress, Long audioId, String audioUrl, String message, String signature   ) {
}

// ai -> 백엔드로 주는 request
// AI 서버가 Job 완료(또는 실패) 후 결과를 백엔드에 보내는 용도.