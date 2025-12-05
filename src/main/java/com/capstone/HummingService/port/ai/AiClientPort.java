package com.capstone.HummingService.port.ai;

import java.io.IOException; // WebClient나 S3 다운로드 실패 시 예외 처리를 위해 추가

/**
 * AI 작곡 모듈과의 통신 포트
 */
public interface AiClientPort {

    /**
     * AI 작곡 모듈에 작곡 요청을 전달하고, 생성된 BGM 파일의 byte 배열 데이터를 반환합니다.
     *
     * @param publicJobId 작업 ID
     * @param hummingStartPath S3에 저장된 허밍 시작 부분 파일 키
     * @param hummingMainPath S3에 저장된 허밍 메인 부분 파일 키
     * @param hummingEndPath S3에 저장된 허밍 끝 부분 파일 키
     * @param imagePath S3에 저장된 시각 자료 파일 키 (Nullable)
     * @param style 스타일
     * @param mood 장르
     * @param durationSec 길이 (초)
     * @param keyInfo 키 정보
     * @param tempoInfo 템포 정보
     * @param instrument 악기 정보 (String 단일 값)
     * @param prompt 텍스트 프롬프트
     * @return 생성된 BGM 파일의 원본 데이터 (byte 배열)
     * @throws IOException S3 다운로드 또는 AI 통신 중 오류 발생 시
     */
    // ⭐️ 반환 타입을 void -> byte[]로 변경하고, IOException을 throw하도록 명시 ⭐️
    byte[] composeWithSongForm(
            String publicJobId,
            String hummingStartPath,
            String hummingMainPath,
            String hummingEndPath,
            String imagePath,
            String style,
            String mood,
            Integer durationSec,
            String keyInfo,
            String tempoInfo,
            String instrument,
            String prompt
    ) throws IOException;
}