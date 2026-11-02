package com.capstone.HummingService.dto.request;

import com.capstone.HummingService.domain.enums.JobStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// AI 서버 → 백엔드: 작업 진행/완료/실패 상태 콜백 (인증은 X-Callback-Secret 헤더로 수행)
public record CallbackRequest(@NotBlank String jobId, @NotNull JobStatus status, Double progress, Long audioId, String message) {
}
