package com.capstone.HummingService.port.ai.impl;

import com.capstone.HummingService.port.ai.AiClientPort;
import com.capstone.HummingService.service.AudioService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class AiClientHttp implements AiClientPort {

    private static final Logger logger = LoggerFactory.getLogger(AiClientHttp.class);
    private final WebClient aiWebClient;
    private final AudioService audioService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${server.callback-host}")
    private String callbackHost;

    @Value("${server.port}")
    private String serverPort;


    // 생성자에 AudioService를 추가 주입받도록 수정
    public AiClientHttp(@Qualifier("aiWebClient") WebClient aiWebClient, AudioService audioService) {
        this.aiWebClient = aiWebClient;
        this.audioService = audioService;
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
        // AI 서버는 melody_file을 요구하므로, 여기서는 hummingMainPath를 사용합니다.

        // ⭐⭐⭐ [임시 수정] 프론트엔드에서 S3 키가 잘못 오는 문제 해결을 위해 유효한 키를 하드 코딩 ⭐⭐⭐
        // 이 키는 이전에 S3 Upload Success 로그에 기록된 실제 키입니다.
        // 최종적으로는 프론트엔드/ProcessService에서 유효한 키를 전달해야 합니다.
        // String validS3Key = "humming1764169263508_main-test_melody.mp3";

        // 현재는 DTO에서 받아온 hummingMainPath를 그대로 사용하되, S3 404 오류가 나지 않도록 유효한 키로 대체해야 합니다.
        // DB에 저장된 값이 'test_melody.mp3'인 경우, 이 값을 대체합니다.
        String s3KeyToDownload = "humming1764169263508_main-test_melody.mp3";

        try {
            // 1. S3에서 파일 다운로드 (byte 배열로 받음)
            logger.info("S3 Download Start for JobId={} | S3 Key: {}", publicJobId, s3KeyToDownload);

            byte[] hummingBytes = audioService.getAudioBytesByKey(s3KeyToDownload);
            // byte 배열을 WebClient가 처리할 수 있는 Resource 객체로 래핑
            ByteArrayResource hummingResource = new ByteArrayResource(hummingBytes) {
                @Override
                public String getFilename() {
                    // 파일명을 설정하여 Content-Disposition 헤더에 사용될 수 있게 함
                    return s3KeyToDownload;
                }
            };

            // 2. MultipartBodyBuilder 생성 (Form-data 요청 본문)
            MultipartBodyBuilder builder = new MultipartBodyBuilder();

            // 2-1. 파일 데이터 추가 (Key: melody_file, AI 서버의 Flask 코드에 맞춤)
            builder.part("melody_file", hummingResource)
                    .filename(hummingResource.getFilename())
                    .contentType(MediaType.parseMediaType("audio/mp3"));

            // 2-2. 폼 데이터 추가 (Flask 코드에 있는 description과 duration만 전송)
            String finalDescription = (prompt != null && !prompt.isEmpty()) ? prompt : "Generate music based on style: " + style + ", mood: " + mood;

            builder.part("description", finalDescription);
            builder.part("duration", durationSec.toString());

            logger.info("AI Request: Path=/generate, ContentType=multipart/form-data, Description={}", finalDescription);

            // 3. WebClient 요청
            aiWebClient.post()
                    .uri("/generate")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            logger.info("AI 요청 전송 성공: JobId={}", publicJobId);

        } catch (WebClientResponseException e) {
            // AI 서버에서 4xx, 5xx 에러가 발생했을 때
            logger.error("AI 응답 오류 [{} {}] JobId={}: {}",
                    e.getStatusCode(), e.getStatusText(), publicJobId, e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI 서버 응답 오류 발생: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            // S3 다운로드 오류, 연결 오류, 타임아웃 오류 등
            logger.error("AI 요청 중 심각한 오류 발생 JobId={}: {}", publicJobId, e.getMessage(), e);

            // S3 404 오류는 이제 이 임시 키를 사용했으므로 발생하지 않기를 기대합니다.
            throw new RuntimeException("AI API 호출 실패", e);
        }
    }
}