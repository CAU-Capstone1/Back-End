package com.capstone.HummingService.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class AwsConfig {

    /**
     * S3Client Bean을 등록합니다.
     * EC2 인스턴스에 할당된 IAM Role과
     * application.properties의 region 설정을 자동으로 사용합니다.
     */
    @Bean
    public S3Client s3Client() {
        // AWS SDK V2의 S3Client를 빌더 패턴으로 생성합니다.
        // 이 빌더는 자동으로 EC2의 IAM Role과 application.properties의 region을 찾습니다.
        return S3Client.builder().build();
    }
}