package com.capstone.HummingService.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;


@Service
public class LocalStorageService {

    private final Path root = Paths.get("storage"); // 허밍파일 업로드한거 로컬 디렉토리에 저장

    public LocalStorageService() throws IOException
    {
        Files.createDirectories(root);
    }


    //Spring에서 업로드된 파일은 자동으로 MultipartFile 타입
    //audio타입이 허밍인지 아웃풋인지 구분하기 위해
    public String save(MultipartFile file, String audioType) throws IOException
    {
        String originName = file.getOriginalFilename();
        if (originName == null)
        {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }


        // 우선은 mp3파일만 가능하게 함
        if (!originName.toLowerCase().endsWith(".mp3"))
        {
            throw new IllegalArgumentException("mp3 파일만 업로드 가능합니다.");
        }


        String saveName = audioType + "_" + UUID.randomUUID().toString().substring(0, 8) + "_" + originName;
        Path path = root.resolve(saveName).normalize();


        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return path.toString();
    }

}
