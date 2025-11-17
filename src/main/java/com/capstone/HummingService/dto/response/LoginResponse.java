package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.User; // User 도메인 임포트

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private User user; // 🚨 [추가] User 객체를 담는 필드

    // 🚨 생성자 수정: User 객체를 인자로 받습니다.
    public LoginResponse(String accessToken, String tokenType, User user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.user = user; // User 객체 초기화
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    // 🚨 User Getter 추가
    public User getUser() {
        return user;
    }
}