package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
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
                                     List<String> hummingPaths,
                                     List<String> songForm,
                                     String imagePath,
                                     String genre,
                                     Integer durationSec,
                                     String keyInfo,
                                     String tempoInfo,
                                     List<String> instruments,
                                     String prompt
    ) {
        Map<String, Object> body = new HashMap<>();

        body.put("jobId", publicJobId);
        body.put("hummingPaths", hummingPaths);
        body.put("songForm", songForm);
        body.put("imagePath", imagePath);

        body.put("genre", genre);
        body.put("durationSec", durationSec);
        body.put("keyInfo", keyInfo);
        body.put("tempoInfo", tempoInfo);
        body.put("instruments", instruments);
        body.put("prompt", prompt);

        body.put("callbackUrl", "http://localhost:8080/callback");

        aiWebClient.post()
                .uri("/compose")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}