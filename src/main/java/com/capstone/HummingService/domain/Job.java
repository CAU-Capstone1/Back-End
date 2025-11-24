package com.capstone.HummingService.domain;

import com.capstone.HummingService.domain.enums.JobStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jobs",
        indexes = {
                @Index(name = "idx_job_process", columnList = "processId"),
                @Index(name = "idx_job_user", columnList = "userId")
        })
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String publicJobId; // 외부적으로 공개할 아이디

    // 이 작업을 요청한 사용자의 ID
    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long processId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'QUEUED'")
    private JobStatus status;

    @Column(nullable = false)
    private double progress;

    private Long outputAudioId; // ai가 만든 음악 아이디

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = JobStatus.QUEUED;
        }
        if (this.progress == 0.0) this.progress = 0.0;
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPublicJobId() {
        return publicJobId;
    }

    public void setPublicJobId(String publicJobId) {
        this.publicJobId = publicJobId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Long getOutputAudioId() {
        return outputAudioId;
    }

    public void setOutputAudioId(Long outputAudioId) {
        this.outputAudioId = outputAudioId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}