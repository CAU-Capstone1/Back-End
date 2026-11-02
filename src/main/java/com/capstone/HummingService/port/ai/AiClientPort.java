package com.capstone.HummingService.port.ai;

public interface AiClientPort {

    // 허밍 멜로디(S3 key)와 텍스트 설명으로 음악을 생성하고, 생성된 WAV 데이터를 반환한다
    byte[] compose(String publicJobId, String melodyS3Key, String description, int durationSec);
}
