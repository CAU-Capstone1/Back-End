package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.HashMap;

@Component
public class AiClientHttp implements AiClientPort {

    private final WebClient aiWebClient;

    public AiClientHttp(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }


    @Override
    public void composeWithSongForm(
            String publicJobId,
            String hummingStartPath,
            String hummingMainPath,
            String hummingEndPath,
            // 이전 List<String> songForm 인자는 ProcessService에서 제거됨
            String imagePath,
            String style,            // genre 대신 style 사용
            Integer durationSec,
            String keyInfo,
            String tempoInfo,
            String instrument,       // List<String> 대신 단일 String 사용
            String prompt
    ) {
        Map<String, Object> body = new HashMap<>();

        body.put("jobId", publicJobId);

        // 프론트 DTO 구조와 일치하는 필드명으로 전송
        body.put("hummingStart", hummingStartPath);
        body.put("hummingMain", hummingMainPath);
        body.put("hummingEnd", hummingEndPath);

        // songForm 관련 필드는 전송하지 않음 (제거)

        body.put("referenceVisual", imagePath); // AI 서버 측 필드명도 referenceVisual로 가정
        body.put("style", style);
        body.put("durationSec", durationSec);
        body.put("key", keyInfo); // AI 서버 측 필드명도 key로 가정
        body.put("tempo", tempoInfo); // AI 서버 측 필드명도 tempo로 가정
        body.put("instrument", instrument);
        body.put("prompt", prompt);

        body.put("callbackUrl", "http://localhost:5001/callback");

        aiWebClient.post()
                .uri("/compose")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}