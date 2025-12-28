package com.capstone.HummingService.dto.response;

import com.capstone.HummingService.domain.User; // User 도메인 임포트

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private User user;

    public LoginResponse(String accessToken, String tokenType, User user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public User getUser() {
        return user;
    }
}