package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.response.UploadResponse;
import com.capstone.HummingService.dto.response.SignedUrlResponse;
import com.capstone.HummingService.service.AudioService;
import com.capstone.HummingService.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class AudioController {

    private final AudioService audioService;
    private final CurrentUserService currentUserService;

    public AudioController(AudioService audioService, CurrentUserService currentUserService) {
        this.audioService = audioService;
        this.currentUserService = currentUserService;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestPart("file") MultipartFile file,
                                 @AuthenticationPrincipal UserDetails principal) throws IOException
    {
        Long userId = currentUserService.getUserId(principal);
        String path = audioService.saveAudio(file, userId);

        return new UploadResponse(path);
    }


    @GetMapping("/audio/signed-url")
    public ResponseEntity<SignedUrlResponse> getSignedUrl(@RequestParam("key") String s3Key,
                                                          @AuthenticationPrincipal UserDetails principal) {

        Long userId = currentUserService.getUserId(principal);

        // 본인이 업로드한 파일만 조회 가능 (생성 결과물은 /api/job/{jobId}로 조회)
        if (!audioService.isUploadedBy(s3Key, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없는 파일입니다.");
        }

        String signedUrl = audioService.generateSignedUrl(s3Key);

        return ResponseEntity.ok(new SignedUrlResponse(signedUrl));
    }
}
