package com.capstone.HummingService.dto.response;

public record UploadResponse( String filePath) {
}

// 사용자가 허밍 파일을 업로드했을 때, 저장된 경로를 돌려줌
// 업로드 성공 후, 이 경로를 나중에 ComposeRequest.hummingPath에 넣어 사용