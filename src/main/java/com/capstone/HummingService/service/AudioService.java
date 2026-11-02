package com.capstone.HummingService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class AudioService {

    private final S3Service s3Service;

    public AudioService(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    public String saveAudio(MultipartFile file, Long userId) throws IOException {
        if (file.getOriginalFilename() == null || !file.getOriginalFilename().toLowerCase().endsWith(".mp3")) {
            throw new IllegalArgumentException("mp3 파일만 업로드할 수 있습니다.");
        }

        String audioPath = s3Service.uploadFile(file, userUploadPrefix("humming/", userId));
        return audioPath;
    }

    // 사용자가 업로드한 허밍은 humming/{userId}/ 아래에 저장해 key만으로 소유자를 확인할 수 있게 한다
    public boolean isUploadedBy(String s3Key, Long userId) {
        if (s3Key == null || s3Key.contains("..")) {
            return false;
        }
        return s3Key.startsWith(userUploadPrefix("humming/", userId));
    }

    private String userUploadPrefix(String directory, Long userId) {
        return directory + userId + "/";
    }


    public byte[] getAudioBytesByKey(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 Key가 없습니다.");
        }

        try {
            return s3Service.downloadFile(s3Key);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public String generateSignedUrl(String s3Key) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 Key가 유효하지 않습니다.");
        }

        try {
            final long DURATION_VALUE = 1;
            final TimeUnit DURATION_UNIT = TimeUnit.HOURS;

            return s3Service.generatePresignedUrl(s3Key, DURATION_VALUE, DURATION_UNIT);
        } catch (RuntimeException e) {
            throw new RuntimeException("Signed URL 생성에 실패했습니다: " + e.getMessage());
        }
    }
}