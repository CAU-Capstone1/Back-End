package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.HashMap;

@Component
public class AiClientHttp implements AiClientPort {

    private final WebClient aiWebClient;


    @Value("${server.callback-host}")
    private String callbackHost;

    @Value("${server.port}")
    private String serverPort;


    public AiClientHttp(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }


    @Override
    public void composeWithSongForm(
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
    ) {
        Map<String, Object> body = new HashMap<>();

        body.put("jobId", publicJobId);

        // 파일 경로는 S3 키(Key)로 전달
        body.put("hummingStart", hummingStartPath);
        body.put("hummingMain", hummingMainPath);
        body.put("hummingEnd", hummingEndPath);


        body.put("referenceVisual", imagePath); // AI 서버 측 필드명도 referenceVisual로 가정
        body.put("style", style);
        body.put("mood", mood);
        body.put("durationSec", durationSec);
        body.put("key", keyInfo);
        body.put("tempo", tempoInfo);
        body.put("instrument", instrument);
        body.put("prompt", prompt);


        String callbackUrl = String.format("http://%s:%s/callback", callbackHost, serverPort);
        body.put("callbackUrl", callbackUrl);


        aiWebClient.post()
                .uri("/compose")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}