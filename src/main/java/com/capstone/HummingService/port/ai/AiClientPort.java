package com.capstone.HummingService.port.ai;

import java.util.List;

public interface AiClientPort {

    /**
     * AI 작곡 모듈에 요청을 전송합니다.
     * @param publicJobId 작업 ID
     * @param hummingPath S3에 저장된 허밍 파일 키
     * @param imagePath S3에 저장된 이미지 파일 키 (Nullable)
     * @param genre 장르
     * @param durationSec 길이 (초)
     * @param keyInfo 키 정보
     * @param tempoInfo 템포 정보
     * @param instruments 악기 목록
     * @param prompt 텍스트 프롬프트
     */
    void compose(
            String publicJobId,
            String hummingPath,
            String imagePath, // ⭐ 새로 추가 (이미지 S3 Key)
            String genre,     // ⭐ 새로 추가 (장르)
            Integer durationSec, // ⭐ 새로 추가 (길이)
            String keyInfo,   // ⭐ 새로 추가 (키)
            String tempoInfo, // ⭐ 새로 추가 (템포)
            List<String> instruments, // ⭐ 새로 추가 (악기 목록)
            String prompt     // ⭐ 새로 추가 (텍스트 프롬프트)
    );
}