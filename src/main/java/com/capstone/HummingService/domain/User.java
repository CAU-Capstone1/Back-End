package com.capstone.HummingService.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.capstone.HummingService.domain.enums.Role;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [필수] 이메일 필드
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // [필수] 사용자명 필드
    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    // [필수] 이름 필드
    @Column(nullable = false, length = 50)
    private String name;

    // 사용자 역할
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(updatable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        this.updateTime = this.createTime;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    protected User() {}

    // [최종 수정] 필수 필드 5가지(email, username, password, name, role)를 생성자에서 초기화합니다.
    public User(String email, String username, String password, String name, Role role) {
        this.email = email; // email 필드 초기화
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    // --- Getters ---
    public Long getId() { return id; }

    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Role getRole() { return role; }

    // --- Setters ---
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setRole(Role role) { this.role = role; }
}