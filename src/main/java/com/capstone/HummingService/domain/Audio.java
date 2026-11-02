package com.capstone.HummingService.domain;

import com.capstone.HummingService.domain.enums.AudioType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audio")
public class Audio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    private Long fileSize;

    private double audioLength;

    private String name;

    private LocalDateTime createTime;

    @Enumerated(EnumType.STRING)
    private AudioType audioType;  // HUMMING or GENERATED


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public double getAudioLength() {
        return audioLength;
    }

    public void setAudioLength(double audioLength) {
        this.audioLength = audioLength;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public AudioType getAudioType() {
        return audioType;
    }

    public void setAudioType(AudioType audioType) {
        this.audioType = audioType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
