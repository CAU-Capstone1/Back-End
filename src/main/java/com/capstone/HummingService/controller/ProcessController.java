package com.capstone.HummingService.controller;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.repository.UserRepository;
import com.capstone.HummingService.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compose")
public class ProcessController {

    private final ProcessService processService;
    private final UserRepository userRepository;

    public ProcessController(ProcessService processService, UserRepository userRepository) {
        this.processService = processService;
        this.userRepository = userRepository;
    }


    /**
     * AI 작곡 요청을 처리합니다. (파일 경로/ID를 JSON Body로 받음)
     * 프론트엔드에서 이미 오디오 파일을 서버에 업로드하고 받은 경로(String)를 사용합니다.
     */
    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE) // JSON 요청으로 변경
    public ResponseEntity<ComposeResponse> composeWithPaths(
            @Valid @RequestBody ComposeRequest request, // @RequestBody로 변경
            @AuthenticationPrincipal UserDetails principal
    ) {
        // MultipartFile 관련 유효성 검사 로직은 DTO의 @NotNull/@NotBlank 검사로 대체되므로 제거

        String username = principal.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        ComposeResponse res = processService.composeWithSongForm(request, userId);

        return ResponseEntity.accepted().body(res);
    }
}