package com.capstone.HummingService.service;

// ✅ V1 Import만 사용
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
// ❌ V2 관련 Import (software.amazon.awssdk.*)는 모두 삭제

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import com.amazonaws.services.s3.model.PutObjectRequest; // V1에서도 PutObjectRequest 사용 가능

@Service
public class S3Service {

    private final AmazonS3 s3Client; // V1 클라이언트

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    // 파일 업로드 메서드 로직 (스크린샷 기반 V1 로직)
    public String uploadFile(MultipartFile file, String folderName) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String s3Key = folderName + "/" + UUID.randomUUID().toString() + extension;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // V1 putObject 호출
        s3Client.putObject(bucket, s3Key, file.getInputStream(), metadata);

        return s3Key;
    }
}