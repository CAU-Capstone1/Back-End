package com.capstone.HummingService.dto.response;

public class LoginResponse {

    private String accessToken;
    private String tokenType;
    private UserResponse user;

    public LoginResponse(String accessToken, String tokenType, UserResponse user) {
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

    public UserResponse getUser() {
        return user;
    }
}
