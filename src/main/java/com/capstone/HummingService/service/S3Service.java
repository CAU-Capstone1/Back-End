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
import software.amazon.awssdk.services.s3.model.ObjectCannedACL; // PublicRead 설정을 위한 Import (주석 처리)

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class S3Service {

    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    // Signed URL 생성을 위한 Presigner 클라이언트
    private final S3Presigner presigner;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
        // S3Client와 동일한 설정으로 Presigner 초기화
        this.presigner = S3Presigner.create();
    }

    /**
     * S3에 파일을 업로드하고, 저장된 파일의 URL 경로를 반환합니다.
     * @param file 업로드할 파일 (MultipartFile)
     * @param directoryPath S3 내에 저장될 디렉토리 경로 (예: "audio/")
     * @return S3에 저장된 객체 키 (Key)
     */
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
                    // ⭐ 과제용 영구 설정을 원할 경우: .acl(ObjectCannedACL.PUBLIC_READ) 추가 ⭐
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("S3 Upload Success: {}", fileKey);

            return fileKey;

        } catch (S3Exception e) {
            log.error("S3 Upload Failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    /**
     * byte 배열 데이터를 S3에 직접 업로드하고, 저장된 파일의 객체 키를 반환합니다.
     * @param bytes 업로드할 파일 데이터 (byte 배열)
     * @param directoryPath S3 내에 저장될 디렉토리 경로 (예: "final-bgm/")
     * @param filename 저장될 파일 이름 (고유해야 함)
     * @param contentType 파일의 MIME 타입 (예: "audio/wav")
     * @return S3에 저장된 객체 키 (Key)
     */
    public String uploadBytes(byte[] bytes, String directoryPath, String filename, String contentType) {
        String fileKey = directoryPath + filename;

        try {
            log.info("S3 Bytes Upload Start: Bucket={} Key={} Size={}", bucketName, fileKey, bytes.length);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(contentType)
                    .contentLength((long) bytes.length)
                    // ⭐ 과제용 영구 설정을 원할 경우: .acl(ObjectCannedACL.PUBLIC_READ) 추가 ⭐
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));

            log.info("S3 Bytes Upload Success: {}", fileKey);

            return fileKey;

        } catch (S3Exception e) {
            log.error("S3 Bytes Upload Failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload byte array to S3: " + e.getMessage());
        }
    }

    /**
     * S3에 저장된 파일의 URL을 생성합니다. (퍼블릭 액세스 차단 시 이 메서드는 사용되지 않음)
     * @param fileKey S3에 저장된 객체 키 (Key)
     * @return 파일에 접근할 수 있는 URL
     */
    public String getFileUrl(String fileKey) {
        String encodedKey = URLEncoder.encode(fileKey, StandardCharsets.UTF_8).replace("+", "%20");

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                bucketName,
                s3Client.serviceClientConfiguration().region().id(),
                encodedKey);
    }

    /**
     * S3에 저장된 파일을 다운로드합니다.
     * @param fileKey S3에 저장된 객체 키 (Key)
     * @return 파일의 바이트 배열
     */
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

    /**
     * S3 Key를 사용하여 파일에 접근할 수 있는 기간 한정 Signed URL을 생성합니다.
     * @param fileKey S3에 저장된 객체 키 (Key)
     * @param durationValue 유효 기간 값 (예: 7)
     * @param durationUnit 유효 기간 단위 (예: TimeUnit.DAYS)
     * @return 생성된 Signed URL 문자열
     */
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