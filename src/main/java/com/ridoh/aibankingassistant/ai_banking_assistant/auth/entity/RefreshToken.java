package com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity;

import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 512)
    private String token;

    @Column(nullable = false, updatable = false)
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 255)
    private String deviceId;

    @Column(length = 255)
    private String deviceName;

    @Column(length = 100)
    private String browser;

    @Column(length = 100)
    private String operatingSystem;

    @Column(length = 100)
    private String deviceClass;

    @Column(length = 100)
    private String ipAddress;

    private LocalDateTime lastUsedAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        lastUsedAt = now;
    }
}