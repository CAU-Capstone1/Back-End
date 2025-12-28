package com.capstone.HummingService.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        this.presigner = S3Presigner.create();
    }

    public String uploadFile(MultipartFile file, String directoryPath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileKey = directoryPath + System.currentTimeMillis() + "_" + originalFilename;

        try {
            log.info("S3 Upload Start: Bucket={} Key={} Size={}", bucketName, fileKey, file.getSize());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("S3 Upload Success: {}", fileKey);

            return fileKey;

        } catch (S3Exception e) {
            log.error("S3 Upload Failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    public String uploadBytes(byte[] bytes, String directoryPath, String filename, String contentType) {
        String fileKey = directoryPath + filename;

        try {
            log.info("S3 Bytes Upload Start: Bucket={} Key={} Size={}", bucketName, fileKey, bytes.length);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            log.info("S3 Bytes Upload Success: {}", fileKey);

            return fileKey;

        } catch (S3Exception e) {
            log.error("S3 Bytes Upload Failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload byte array to S3: " + e.getMessage());
        }
    }

    public String getFileUrl(String fileKey) {
        String encodedKey = URLEncoder.encode(fileKey, StandardCharsets.UTF_8).replace("+", "%20");

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                s3Client.serviceClientConfiguration().region().id(),
                encodedKey);
    }

    public byte[] downloadFile(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to download file from S3: " + e.getMessage());
        }
    }

    public String generatePresignedUrl(String fileKey, long durationValue, TimeUnit durationUnit) {
        try {
            // 1. GetObject 요청 구성
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            // 2. 입력받은 durationValue와 durationUnit을 사용하여 Duration 객체 생성
            long seconds = durationUnit.toSeconds(durationValue);
            Duration signatureDuration = Duration.ofSeconds(seconds);

            // 3. Presign 요청 구성
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(signatureDuration) // 동적으로 설정된 유효 시간 적용
                    .getObjectRequest(getObjectRequest)
                    .build();

            // 4. Presigned URL 생성
            PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toExternalForm();

            log.info("Presigned URL generated for Key: {} (Duration: {} {})", fileKey, durationValue, durationUnit.toString());

            return presignedUrl;

        } catch (S3Exception e) {
            log.error("Failed to generate Presigned URL for Key: {}", fileKey, e);
            throw new RuntimeException("Failed to generate Presigned URL: " + e.getMessage());
        }
    }
}