package com.capstone.HummingService.domain;

import com.capstone.HummingService.domain.enums.ProcessStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "compose_processes",
        indexes = {
                @Index(name = "idx_cp_audio",  columnList = "audioId")
        })
public class ComposeProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI가 생성한 최종 결과물 Audio ID
    private Long audioId;

    // ✅ 파일 경로 필드 추가 (S3 Key 저장용)
    @Column(nullable = false)
    private String hummingPath;

    private String imagePath; // 선택적 이미지 파일 경로 (Nullable)

    // ❌ 기존 style, mood 제거하고 6가지 요소 추가
    @Column(length = 50)
    private String genre;

    private Integer durationSec; // 길이 (초)

    @Column(length = 30)
    private String keyInfo; // 키 정보 (C Major, 밝은 계열 등)

    @Column(length = 30)
    private String tempoInfo; // 템포 정보 (빠르게, 120BPM 등)

    @Lob // 길이가 길어질 수 있는 문자열 저장 (DB의 TEXT 타입에 해당)
    private String instruments; // 악기 목록 (JSON 또는 쉼표 구분 문자열로 저장)

    @Column(columnDefinition = "TEXT") // 텍스트 프롬프트 저장
    private String prompt;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'CREATED'")
    private ProcessStatus status;

    @Column(length = 40)
    private String lastJobId;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    // --- Life Cycle Hooks ---

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

    // --- Getter and Setter ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAudioId() { return audioId; }
    public void setAudioId(Long audioId) { this.audioId = audioId; }

    // ✅ S3 경로 Getter/Setter
    public String getHummingPath() { return hummingPath; }
    public void setHummingPath(String hummingPath) { this.hummingPath = hummingPath; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    // ✅ 6가지 음악 요소 Getter/Setter
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public Integer getDurationSec() { return durationSec; }
    public void setDurationSec(Integer durationSec) { this.durationSec = durationSec; }

    public String getKeyInfo() { return keyInfo; }
    public void setKeyInfo(String keyInfo) { this.keyInfo = keyInfo; }

    public String getTempoInfo() { return tempoInfo; }
    public void setTempoInfo(String tempoInfo) { this.tempoInfo = tempoInfo; }

    public String getInstruments() { return instruments; }
    public void setInstruments(String instruments) { this.instruments = instruments; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    // 기존 필드 유지
    public ProcessStatus getStatus() { return status; }
    public void setStatus(ProcessStatus status) { this.status = status; }

    public String getLastJobId() { return lastJobId; }
    public void setLastJobId(String lastJobId) { this.lastJobId = lastJobId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}