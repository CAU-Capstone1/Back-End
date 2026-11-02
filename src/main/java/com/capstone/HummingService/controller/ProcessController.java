package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.service.CurrentUserService;
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
    private final CurrentUserService currentUserService;

    public ProcessController(ProcessService processService, CurrentUserService currentUserService) {
        this.processService = processService;
        this.currentUserService = currentUserService;
    }


    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ComposeResponse> composeWithPaths(
            @Valid @RequestBody ComposeRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = currentUserService.getUserId(principal);

        ComposeResponse res = processService.composeWithSongForm(request, userId);

        return ResponseEntity.accepted().body(res);
    }
}