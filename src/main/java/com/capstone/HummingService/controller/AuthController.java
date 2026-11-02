package com.capstone.HummingService.controller;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.dto.request.LoginRequest;
import com.capstone.HummingService.dto.response.LoginResponse;
import com.capstone.HummingService.dto.request.RegisterRequest;
import com.capstone.HummingService.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);

            LoginRequest loginRequest = new LoginRequest(request.getEmail(), request.getPassword());
            LoginResponse response = authService.authenticate(loginRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
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

        } catch (IllegalArgumentException | AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}