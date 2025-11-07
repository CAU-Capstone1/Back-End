package com.capstone.HummingService.port.ai;

import java.util.List;

public interface AiClientPort {

    /**

     * @param publicJobId 작업 ID
     * @param hummingPaths S3에 저장된 다중 허밍 파일 키 목록 (송폼 순서대로)
     * @param songForm 사용자가 선택한 곡 구조 순서 (Intro, Verse, Chorus 등)
     * @param imagePath S3에 저장된 이미지 파일 키 (Nullable)
     * @param genre 장르
     * @param durationSec 길이 (초)
     * @param keyInfo 키 정보
     * @param tempoInfo 템포 정보
     * @param instruments 악기 목록
     * @param prompt 텍스트 프롬프트
     */
    void composeWithSongForm(
            String publicJobId,
            List<String> hummingPaths,
            List<String> songForm,
            String imagePath,
            String genre,
            Integer durationSec,
            String keyInfo,
            String tempoInfo,
            List<String> instruments,
            String prompt
    );
}