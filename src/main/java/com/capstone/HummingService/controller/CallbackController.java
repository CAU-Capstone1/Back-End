package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.request.CallbackRequest;
import com.capstone.HummingService.service.CallbackService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/callback")
public class CallbackController {

    private final CallbackService callbackService;

    public CallbackController(CallbackService callbackService) {
        this.callbackService = callbackService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> handle(@Valid @RequestBody CallbackRequest request) {
        // TODO: request.signature 검증 (HMAC 등)
        callbackService.handleCallback(request);
        return ResponseEntity.ok().build();
    }
}
