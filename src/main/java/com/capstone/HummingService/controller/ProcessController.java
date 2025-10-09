package com.capstone.HummingService.controller;

import com.capstone.HummingService.dto.request.ComposeRequest;
import com.capstone.HummingService.dto.response.ComposeResponse;
import com.capstone.HummingService.service.ProcessService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/compose")
public class ProcessController {

        private final ProcessService processService;

        public ProcessController(ProcessService processService) {
            this.processService = processService;
        }


        @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<ComposeResponse> compose(@Valid @RequestBody ComposeRequest request) {
            ComposeResponse res = processService.compose(request);
            return ResponseEntity.accepted().body(res);
        }
    }

