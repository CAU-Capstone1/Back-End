package com.capstone.HummingService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

        // ⭐ S3Service의 uploadFile 메서드 사용
        String audioPath = s3Service.uploadFile(file, "humming");
        return audioPath;
    }

    // ⭐ 이미지 저장을 위한 새 메소드 추가 (선택 사항)
    public String saveImage(MultipartFile file) throws IOException {
        // 이미지 파일 검증 로직 추가 가능 (예: .jpg, .png 등)

        // ⭐ S3Service의 uploadFile 메서드 사용
        String imagePath = s3Service.uploadFile(file, "image");
        return imagePath;
    }
}