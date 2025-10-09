package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class AiClientHttp implements AiClientPort {

    private final WebClient aiWebClient;

    public AiClientHttp(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    @Override
    public void compose(String publicJobId, String hummingPath, String mood, String style) {
        Map<String, Object> body = Map.of(
                "jobId", publicJobId,
                "hummingPath", hummingPath,
                "mood", mood,
                "style", style,
                "callbackUrl", "http://localhost:8080/callback"
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
