package com.capstone.HummingService.port.ai;

// import java.util.List; // List는 이제 사용하지 않으므로 제거

/**
 * AI 작곡 모듈과의 통신 포트
 */
public interface AiClientPort {

    /**
     * AI 작곡 모듈에 작곡 요청을 전달합니다.
     * DTO 변경사항에 따라 파일 경로(String)와 단일 설정 값들을 전달합니다.
     *
     * @param publicJobId 작업 ID
     * @param hummingStartPath S3에 저장된 허밍 시작 부분 파일 키
     * @param hummingMainPath S3에 저장된 허밍 메인 부분 파일 키
     * @param hummingEndPath S3에 저장된 허밍 끝 부분 파일 키
     * @param imagePath S3에 저장된 시각 자료 파일 키 (Nullable)
     * @param style 장르/스타일 (기존 genre에서 변경)
     * @param durationSec 길이 (초)
     * @param keyInfo 키 정보
     * @param tempoInfo 템포 정보
     * @param instrument 악기 정보 (기존 List<String>에서 String 단일 값으로 변경)
     * @param prompt 텍스트 프롬프트
     */
    void composeWithSongForm(
            String publicJobId,
            String hummingStartPath, // 변경됨
            String hummingMainPath,  // 변경됨
            String hummingEndPath,   // 변경됨
            // List<String> songForm 인자 제거
            String imagePath,
            String style,            // genre -> style
            Integer durationSec,
            String keyInfo,
            String tempoInfo,
            String instrument,       // List<String> -> String
            String prompt
    );
}