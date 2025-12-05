package com.capstone.HummingService.dto.response;

public class SignedUrlResponse {
    private String signedUrl;

    public SignedUrlResponse(String signedUrl) {
        this.signedUrl = signedUrl;
    }

    public String getSignedUrl() {
        return signedUrl;
    }
    public void setSignedUrl(String signedUrl) {
        this.signedUrl = signedUrl;
    }

}