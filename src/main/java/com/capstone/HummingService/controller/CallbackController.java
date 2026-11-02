package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.request.CallbackRequest;
import com.capstone.HummingService.service.CallbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/callback")
public class CallbackController {

    private static final String SECRET_HEADER = "X-Callback-Secret";

    private final CallbackService callbackService;
    private final String callbackSecret;

    public CallbackController(CallbackService callbackService,
                              @Value("${ai.callback-secret:}") String callbackSecret) {
        this.callbackService = callbackService;
        this.callbackSecret = callbackSecret;
    }

    // AI 서버만 호출할 수 있도록 공유 시크릿 헤더를 검증 (시크릿 미설정 시 모든 콜백 거부)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> handle(@RequestHeader(value = SECRET_HEADER, required = false) String secret,
                                       @Valid @RequestBody CallbackRequest request) {
        if (!isValidSecret(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        callbackService.handleCallback(request);
        return ResponseEntity.ok().build();
    }

    private boolean isValidSecret(String secret) {
        if (callbackSecret.isBlank() || secret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                callbackSecret.getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8));
    }
}
