package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.response.UploadResponse;
// ⭐ Signed URL 응답을 위한 DTO 추가 (필요)
import com.capstone.HummingService.dto.response.SignedUrlResponse;
import com.capstone.HummingService.service.AudioService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity; // ResponseEntity 사용을 위해 추가
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AudioController {

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    /**
     * 1. 허밍 파일 업로드 엔드포인트 (기존 코드)
     * POST /api/upload
     */
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestPart("file") MultipartFile file) throws IOException
    {
        // 1. AudioService를 통해 S3에 파일을 저장하고, S3 Key를 반환받음
        String path = audioService.saveAudio(file);

        // 2. 클라이언트에게 S3 Key를 담아 응답
        return new UploadResponse(path);
    }

    // ⭐⭐⭐ 2. S3 Key를 받아 Signed URL을 반환하는 재생용 엔드포인트 추가 (핵심) ⭐⭐⭐
    /**
     * 최종 BGM 파일을 재생하기 위해, S3 Key를 받아 Signed URL을 반환합니다.
     * GET /api/audio/signed-url?key={s3Key}
     */
    @GetMapping("/audio/signed-url")
    public ResponseEntity<SignedUrlResponse> getSignedUrl(@RequestParam("key") String s3Key) {
        // 1. AudioService에 S3 Key를 전달하여 Signed URL 생성 요청
        //    (AudioService 내부에는 S3 Presigner를 사용하는 로직이 필요함)
        String signedUrl = audioService.generateSignedUrl(s3Key);

        // 2. 응답 DTO를 사용하여 클라이언트에 URL 반환
        return ResponseEntity.ok(new SignedUrlResponse(signedUrl));
    }
    // ⭐⭐⭐ 추가된 엔드포인트 끝 ⭐⭐⭐
}

// ⭐ UploadResponse와 유사하게 SignedUrlResponse DTO가 정의되어야 합니다.
// public record SignedUrlResponse(String url) {}