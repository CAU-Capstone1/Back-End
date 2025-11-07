package com.capstone.HummingService.dto.request; // RegisterRequest와 동일한 패키지 사용

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 데이터 (LoginRequest)
 */
public class LoginRequest {

    @NotBlank(message = "사용자명(ID)은 필수 입력 항목입니다.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;

    public LoginRequest() {
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}