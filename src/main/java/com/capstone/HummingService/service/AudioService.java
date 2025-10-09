package com.capstone.HummingService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AudioService {

    private final LocalStorageService localStorageService;

    public AudioService(LocalStorageService localStorageService) {
        this.localStorageService = localStorageService;
    }


    public String saveAudio(MultipartFile file) throws IOException {

        if (!file.getOriginalFilename().toLowerCase().endsWith(".mp3"))
        {
            throw new IllegalArgumentException("mp3 파일만 업로드할 수 있습니다.");
        }

        String savedPath = localStorageService.save(file,"humming");
        return savedPath;


    }




}
