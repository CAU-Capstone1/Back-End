package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // ⭐ 새로 추가

@RestController
@RequestMapping("/api/compose")
public class ProcessController {

    private final ProcessService processService;

    public ProcessController(ProcessService processService) {
        this.processService = processService;
    }

    // 기존 compose 엔드포인트 유지 (필요 시)
    // ...

    /**
     * ✅ 새로 추가된 엔드포인트: 파일(허밍/이미지)과 메타데이터(6가지 음악적 요소)를 동시에 받아 처리
     * URL: POST /api/compose/full-upload
     * Content-Type: multipart/form-data
     */
    @PostMapping(path = "/full-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComposeResponse> composeWithUpload(
            // 1. 허밍 파일 (필수)
            @RequestPart("hummingFile") MultipartFile hummingFile,

            // 2. 이미지 파일 (선택 사항)
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,

            // 3. 6가지 음악적 요소가 담긴 DTO (JSON 파트로 받음)
            @Valid @RequestPart("composeRequest") ComposeRequest request
    ) {
        // 이 로직은 ProcessService로 이동하여 S3 업로드, DB 저장 및 AI 호출을 처리합니다.
        ComposeResponse res = processService.composeFull(hummingFile, imageFile, request);

        // HTTP 202 Accepted 응답
        return ResponseEntity.accepted().body(res);
    }
}