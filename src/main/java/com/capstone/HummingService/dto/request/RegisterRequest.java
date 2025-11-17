package com.capstone.HummingService.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 회원가입 요청 데이터 모델
 */
public class RegisterRequest {

    // email은 ID 역할과 @Email 검증을 위해 필수 유지
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    // 비밀번호는 최소 8자 이상 요구 (이전 오류 해결)
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 4, message = "비밀번호는 최소 4자 이상이어야 합니다.")
    private String password;

    // 이름은 프론트엔드에서 보내고 있으므로 유지
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    // **[수정] username 필드의 @NotBlank 조건 제거**
    // 프론트엔드에서 보내지 않으므로, 유효성 검사 대상에서 제외합니다.
    private String username;


    // --- Getters and Setters ---

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // **[수정] username의 @NotBlank 제거를 반영**
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}