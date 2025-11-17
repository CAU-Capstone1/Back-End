package com.capstone.HummingService.dto.request; // RegisterRequest와 동일한 패키지 사용

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 데이터 (LoginRequest)
 * - 사용자명 대신 이메일로 로그인하도록 수정
 */
public class LoginRequest {

    // [수정] username 대신 email 필드 사용
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.") // 이메일 형식 검증 추가
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    public LoginRequest() {
    }

    // [수정] getUsername/setUsername 대신 getEmail/setEmail 사용
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}