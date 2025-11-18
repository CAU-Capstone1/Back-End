package com.capstone.HummingService.controller;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.dto.request.LoginRequest;
import com.capstone.HummingService.dto.response.LoginResponse;
import com.capstone.HummingService.dto.request.RegisterRequest;
import com.capstone.HummingService.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    // 🚨 [수정] 응답 타입을 LoginResponse로 변경하고, 회원가입 후 자동 로그인 처리
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // 1. 회원가입 처리 (DB 저장)
            User user = authService.register(request);

            // 2. 자동 로그인 처리 및 토큰 발급
            // LoginRequest를 수동 생성하여 authenticate를 호출합니다.
            // (LoginRequest에 email, password를 인자로 받는 생성자가 있다고 가정합니다.)
            LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
            LoginResponse response = authService.authenticate(loginRequest);

            // 3. 201 Created와 LoginResponse 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // 이메일 중복 등의 오류 시 400 Bad Request 처리
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}