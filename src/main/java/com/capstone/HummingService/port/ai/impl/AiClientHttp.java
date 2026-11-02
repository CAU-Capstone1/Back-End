package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import com.capstone.HummingService.service.AudioService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AiClientHttp implements AiClientPort {

    private static final Logger logger = LoggerFactory.getLogger(AiClientHttp.class);
    private final WebClient aiWebClient;
    private final AudioService audioService;

    public AiClientHttp(@Qualifier("aiWebClient") WebClient aiWebClient, AudioService audioService) {
        this.aiWebClient = aiWebClient;
        this.audioService = audioService;
    }

    @Override
    public byte[] compose(String publicJobId, String melodyS3Key, String description, int durationSec) {
        try {
            // 1. S3에서 파일 다운로드 (byte 배열로 받음)
            logger.info("S3 Download Start for JobId={} | S3 Key: {}", publicJobId, melodyS3Key);
            byte[] hummingBytes = audioService.getAudioBytesByKey(melodyS3Key);
            ByteArrayResource hummingResource = new ByteArrayResource(hummingBytes) {
                @Override
                public String getFilename() {
                    return melodyS3Key;
                }
            };

            // 2. MultipartBodyBuilder 생성 및 폼 데이터 추가
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("melody_file", hummingResource)
                    .filename(hummingResource.getFilename())
                    .contentType(MediaType.parseMediaType("audio/mp3"));

            builder.part("description", description);
            builder.part("duration", String.valueOf(durationSec));

            logger.info("AI Request: Path=/generate, ContentType=multipart/form-data, Description={}", description);

            // 3. WebClient 요청 및 응답 데이터 처리
            byte[] bgmFileData = aiWebClient.post()
                    .uri("/generate")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();

            if (bgmFileData == null || bgmFileData.length == 0) {
                throw new IllegalStateException("AI 서버가 빈 응답을 반환했습니다.");
            }

            logger.info("AI 요청 전송 및 BGM 데이터 수신 성공: JobId={} | Data Size={}", publicJobId, bgmFileData.length);

            return bgmFileData;

        } catch (WebClientResponseException e) {
            // AI 서버에서 4xx, 5xx 에러가 발생했을 때
            logger.error("AI 응답 오류 [{} {}] JobId={}: {}",
                    e.getStatusCode(), e.getStatusText(), publicJobId, e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI 서버 응답 오류 발생: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            // S3 다운로드 오류, 연결 오류 등
            logger.error("AI 요청 중 심각한 오류 발생 JobId={}: {}", publicJobId, e.getMessage(), e);
            throw new RuntimeException("AI API 호출 실패", e);
        }
    }
}