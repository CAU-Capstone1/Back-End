package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List; // ⭐ 새로 추가
import java.util.Map;

@Component
public class AiClientHttp implements AiClientPort {

    private final WebClient aiWebClient;

    public AiClientHttp(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    @Override
    public void compose(
            String publicJobId,
            String hummingPath,
            String imagePath, // ⭐ 추가
            String genre,     // ⭐ 추가
            Integer durationSec, // ⭐ 추가
            String keyInfo,   // ⭐ 추가
            String tempoInfo, // ⭐ 추가
            List<String> instruments, // ⭐ 추가
            String prompt     // ⭐ 추가
    ) {
        // AI 서버로 전송할 JSON 본문 (Map) 구성
        Map<String, Object> body = Map.of(
                "jobId", publicJobId,
                "hummingPath", hummingPath,
                "imagePath", imagePath, // ⭐ 이미지 경로 추가

                // ⭐ 6가지 음악적 요소 및 프롬프트 추가
                "genre", genre,
                "durationSec", durationSec,
                "keyInfo", keyInfo,
                "tempoInfo", tempoInfo,
                "instruments", instruments,
                "prompt", prompt,

                "callbackUrl", "http://localhost:8080/callback" // 콜백 URL은 유지
        );

        aiWebClient.post()
                .uri("/compose")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}