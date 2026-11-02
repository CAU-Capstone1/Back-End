package com.capstone.HummingService.domain.enums;

public enum ProcessStatus {

    CREATED,      // 프로세스 생성(업로드 직후)
    PROCESSING,   // 전처리/AI 실행 중
    DONE,         // 생성 완료
    FAILED        // 실패
}
