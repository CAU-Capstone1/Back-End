package com.capstone.HummingService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    /**
     * S3Client Bean을 등록하고, 리전을 ap-northeast-2로 강제 지정합니다.
     * 이렇게 하면 Spring의 설정 파일에 의존하지 않고 정확한 S3 엔드포인트를 사용합니다.
     */
    @Bean
    public S3Client s3Client() {
        // AWS SDK V2의 S3Client를 빌더 패턴으로 생성하며, 리전을 서울(AP_NORTHEAST_2)로 지정
        return S3Client.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전으로 강제 지정
                .build();
    }
}