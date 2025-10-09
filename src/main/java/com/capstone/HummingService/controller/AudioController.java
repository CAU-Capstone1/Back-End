package com.capstone.HummingService.controller;

import com.capstone.HummingService.domain.Audio;
import com.capstone.HummingService.dto.response.UploadResponse;
import com.capstone.HummingService.service.AudioService;
import com.capstone.HummingService.service.LocalStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AudioController {

    private final AudioService audioService;

    public AudioController(AudioService audioService) {
        this.audioService = audioService;
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestPart("file") MultipartFile file) throws IOException
    {
        String path = audioService.saveAudio(file);
        return new UploadResponse(path);
    }
}
