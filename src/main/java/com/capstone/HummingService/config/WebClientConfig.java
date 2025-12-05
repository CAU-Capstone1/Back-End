package com.capstone.HummingService.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeStrategies; // ⬅️ 이 import를 추가해야 합니다.
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("aiWebClient")
    public WebClient aiWebClient(@Value("${ai.base-url:http://localhost:8000}") String baseUrl) {

        // 1. 최대 버퍼 크기 설정 (예: 10MB)
        final int size = 10 * 1024 * 1024;

        // 2. ExchangeStrategies 객체 생성 및 설정 적용
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(size))
                .build();

        // 3. WebClient 빌더에 ExchangeStrategies 적용
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchangeStrategies(strategies) // ⬅️ 이 부분을 추가해야 합니다.
                .build();
    }
}