package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.enums.JobStatus;

// 클라이언트가 특정 Job의 진행 상태를 조회할 때 받습니다.
public record JobStatusResponse(
        String jobId,
        JobStatus status,
        double progress,
        String errorMessage,
        String musicUrl // ⬅️ AI 음악 파일의 접근 가능한 URL (완료 시에만 제공)
) {
}