package com.capstone.HummingService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
public class AudioService {

    // ⭐ LocalStorageService 대신 S3Service 사용
    private final S3Service s3Service;

    // ⭐ 생성자 수정
    public AudioService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public String saveAudio(MultipartFile file) throws IOException {
        // mp3 확장자 검사는 유지
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("mp3 파일만 업로드할 수 있습니다.");
        }

        String audioPath = s3Service.uploadFile(file, "humming");
        return audioPath;
    }

    public String saveImage(MultipartFile file) throws IOException {

        String imagePath = s3Service.uploadFile(file, "image");
        return imagePath;
    }

    /**
     * S3에 저장된 파일을 다운로드하여 바이트 배열을 반환합니다.
     * AiClientHttp에서 AI 서버로 파일을 전송하기 위해 사용됩니다.
     * @param s3Key S3에 저장된 파일의 키 (경로)
     * @return S3 파일 데이터를 담은 byte 배열
     */
    public byte[] getAudioBytesByKey(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 Key가 없습니다.");
        }

        try {
            // S3Service의 downloadFile 메서드를 직접 호출하여 byte[]를 반환합니다.
            return s3Service.downloadFile(s3Key);
        } catch (RuntimeException e) {
            // S3Service에서 발생한 RuntimeException (S3Exception 포함)을 그대로 전달합니다.
            throw e;
        }
    }
}