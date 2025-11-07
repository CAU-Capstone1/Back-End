package com.capstone.HummingService.controller;

import com.capstone.HummingService.domain.User;
import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.repository.UserRepository;
import com.capstone.HummingService.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/compose")
public class ProcessController {

    private final ProcessService processService;
    private final UserRepository userRepository;
    public ProcessController(ProcessService processService, UserRepository userRepository) {
        this.processService = processService;
        this.userRepository = userRepository;
    }


    @PostMapping(path = "/full-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComposeResponse> composeWithUpload(
            @Valid @ModelAttribute ComposeRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        if (request.getHummingFiles() == null || request.getSongForm() == null ||
                request.getHummingFiles().size() != request.getSongForm().size() ||
                request.getHummingFiles().stream().anyMatch(MultipartFile::isEmpty)) {
            return ResponseEntity.badRequest().build();
        }

        String username = principal.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("인증된 사용자를 찾을 수 없습니다."));

        Long userId = user.getId();

        ComposeResponse res = processService.composeWithSongForm(request, userId);

        return ResponseEntity.accepted().body(res);
    }
}