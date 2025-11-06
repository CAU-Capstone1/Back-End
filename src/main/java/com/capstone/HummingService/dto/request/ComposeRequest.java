package com.capstone.HummingService.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.List;

public record ComposeRequest(
        // Step 2. 장르 선택
        @NotBlank(message = "장르를 선택해 주세요.")
        String genre,

        // Step 3. 길이 선택 (초 단위)
        @NotNull(message = "길이를 입력해 주세요.")
        @Min(value = 5, message = "최소 5초 이상이어야 합니다.")
        Integer durationSec,

        // Step 4. 키 선택
        @NotBlank(message = "키 정보를 입력해 주세요.")
        String key,

        // Step 5. 템포 선택
        @NotBlank(message = "템포 정보를 선택해 주세요.")
        String tempo,

        // Step 6. 악기 선택
        @NotNull(message = "사용할 악기를 최소 하나 이상 선택해 주세요.")
        List<String> instruments,

        // Step 2/6. 텍스트 입력 (선택적)
        String imagePrompt

        // 파일 경로는 이제 이 DTO를 사용하는 서비스 메소드 인자로 전달되거나
        // 서비스 내부에서 최종 DTO(AI 전달용)로 조합됩니다.
) {
}