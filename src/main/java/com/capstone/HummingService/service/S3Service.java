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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class S3Service {

    // S3 작업 로그를 위한 로거 필드
    private static final Logger log = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;

    // application.properties에서 설정한 버킷 이름을 주입받습니다.
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * S3에 파일을 업로드하고, 저장된 파일의 URL 경로를 반환합니다.
     * @param file 업로드할 파일 (MultipartFile)
     * @param directoryPath S3 내에 저장될 디렉토리 경로 (예: "audio/")
     * @return S3에 저장된 객체 키 (Key)
     */
    public String uploadFile(MultipartFile file, String directoryPath) throws IOException {
        String originalFilename = file.getOriginalFilename();
        // 파일 이름 충돌을 피하기 위해 타임스탬프를 추가하여 고유한 키를 생성합니다.
        String fileKey = directoryPath + System.currentTimeMillis() + "_" + originalFilename;

        try {
            // S3 업로드 시작 정보를 로그로 출력
            log.info("S3 Upload Start: Bucket={} Key={} Size={}", bucketName, fileKey, file.getSize());

            // S3 PutObject 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // 파일 데이터를 요청 본문으로 전송 (실제 업로드 실행)
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // S3 업로드 성공 로그 출력
            log.info("S3 Upload Success: {}", fileKey);

            // 데이터베이스에 저장할 파일 키 반환
            return fileKey;

        } catch (S3Exception e) {
            // S3 관련 예외 처리 (권한, 버킷 없음 등)
            // 에러 로그에 상세 메시지와 스택 트레이스를 포함하여 출력
            log.error("S3 Upload Failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }
    }

    /**
     * S3에 저장된 파일의 URL을 생성합니다.
     * @param fileKey S3에 저장된 객체 키 (Key)
     * @return 파일에 접근할 수 있는 URL (퍼블릭 액세스 차단 시 내부 접근용 URL)
     */
    public String getFileUrl(String fileKey) {
        // 객체 키에 특수 문자가 포함될 경우를 대비하여 인코딩합니다.
        // URL 인코딩 시 ' ' (공백)이 '+'로 변환되는데, S3 URL에서는 '%20'으로 유지해야 하므로 replace 처리합니다.
        String encodedKey = URLEncoder.encode(fileKey, StandardCharsets.UTF_8).replace("+", "%20");

        // S3 버킷의 기본 URL 형식에 맞추어 URL을 생성합니다.
        // 리전은 S3Client 설정(serviceClientConfiguration)에서 가져옵니다.
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

            // S3Client의 getObjectAsBytes를 사용하여 파일 데이터를 바이트 배열로 가져옵니다.
            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (S3Exception e) {
            throw new RuntimeException("Failed to download file from S3: " + e.getMessage());
        }
    }
}