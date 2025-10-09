package com.capstone.HummingService.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ComposeRequest(@NotBlank String hummingPath,@NotBlank String mood, @NotBlank String style  )
{

}

// 프론트엔드 -> 백엔드
// 사용자의 입력(허밍, 분위기, 스타일)을 받아서 새로운 ComposeProcess를 생성하고, 그에 대응하는 Job을 큐에 등록시킴.
// 여기서 humingpath = 사용자가 업로드한 허밍 파일의 경로 (Audio.filePath)