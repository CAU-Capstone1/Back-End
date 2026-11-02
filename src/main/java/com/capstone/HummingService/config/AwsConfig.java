package com.capstone.HummingService.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class AwsConfig {

    // 비어 있으면 실제 AWS S3 사용, 값이 있으면 해당 엔드포인트(S3Mock 등 S3 호환 스토리지) 사용
    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    private final AwsCredentialsProvider credentialsProvider;

    // spring.cloud.aws.credentials.* 설정이 있으면 그 값을, 없으면 기본 체인(EC2 IAM Role 등)을 사용
    public AwsConfig(ObjectProvider<AwsCredentialsProvider> credentialsProvider) {
        this.credentialsProvider = credentialsProvider.getIfAvailable(DefaultCredentialsProvider::create);
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.AP_NORTHEAST_2) // 서울 리전으로 강제 지정
                .credentialsProvider(credentialsProvider);

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint)).forcePathStyle(true);
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        var builder = S3Presigner.builder()
                .region(Region.AP_NORTHEAST_2) // S3Client와 같은 리전 사용
                .credentialsProvider(credentialsProvider);

        if (!endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint))
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }
        return builder.build();
    }
}
