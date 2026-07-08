package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SessionResponse {

    private UUID sessionId;

    private String deviceName;

    private String browser;

    private String operatingSystem;

    private String deviceClass;

    private String ipAddress;

    private LocalDateTime createdAt;

    private LocalDateTime lastUsedAt;

    private LocalDateTime expiresAt;

    private boolean currentSession;

    private boolean revoked;
}