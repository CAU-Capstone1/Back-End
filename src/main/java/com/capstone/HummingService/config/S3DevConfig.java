package com.capstone.HummingService.config;

import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Profile("dev")
@Configuration
public class S3DevConfig {

    @Bean
    public S3Client s3Client() {
        return mock(S3Client.class);
    }
}