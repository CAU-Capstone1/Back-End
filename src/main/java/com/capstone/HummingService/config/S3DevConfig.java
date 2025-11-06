package com.capstone.HummingService.config;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// AWS 연결이 안 된 로컬 개발 환경에서만 이 설정을 로드하도록 합니다.
@Profile("dev")
@Configuration
public class S3DevConfig {

    @Bean
    public AmazonS3 amazonS3() {
        // 실제 AWS 연결이 아닌, null 또는 Mock 객체를 반환하여
        // S3Service가 실행될 수 있도록 빈을 강제로 등록합니다.
        // 실제 운영 환경에서는 이 빈이 로드되지 않아야 합니다.
        return null; // 실행 시 NullPointerException을 피하기 위해 Mockito를 사용하거나
        // S3Service에서 Null 체크를 해야 합니다.
    }
}