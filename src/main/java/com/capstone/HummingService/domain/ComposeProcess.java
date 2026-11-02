package com.capstone.HummingService.domain;

import com.capstone.HummingService.domain.enums.ProcessStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "compose_processes",
        indexes = {
                @Index(name = "idx_cp_audio",  columnList = "audioId"),
                @Index(name = "idx_cp_user",  columnList = "userId")
        })
public class ComposeProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI가 생성한 최종 결과물 Audio ID
    private Long audioId;

    // 이 요청을 생성한 사용자의 ID
    @Column(nullable = false)
    private Long userId;

    // 다중 허밍 파일 경로
    @Column(nullable = false)
    private String hummingPath;

    private String imagePath;

    @Column(length = 50)
    private String style;

    @Column(length = 50)
    private String mood;


    private Integer durationSec; // 길이

    @Column(length = 30)
    private String keyInfo; // 키 정보

    @Column(length = 30)
    private String tempoInfo; // 템포 정보

    @Lob
    private String instruments; // 악기 목록


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'CREATED'")
    private ProcessStatus status;

    @Column(length = 40)
    private String lastJobId;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ProcessStatus.CREATED;
        }
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAudioId() { return audioId; }
    public void setAudioId(Long audioId) { this.audioId = audioId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getHummingPath() { return hummingPath; }
    public void setHummingPath(String hummingPath) { this.hummingPath = hummingPath; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }

    public String getKeyInfo() { return keyInfo; }
    public void setKeyInfo(String keyInfo) { this.keyInfo = keyInfo; }

    public String getTempoInfo() { return tempoInfo; }
    public void setTempoInfo(String tempoInfo) { this.tempoInfo = tempoInfo; }

    public String getInstruments() { return instruments; }
    public void setInstruments(String instruments) { this.instruments = instruments; }

    public ProcessStatus getStatus() { return status; }
    public void setStatus(ProcessStatus status) { this.status = status; }

    public String getLastJobId() { return lastJobId; }
    public void setLastJobId(String lastJobId) { this.lastJobId = lastJobId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}