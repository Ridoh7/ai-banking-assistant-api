package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SessionInfo {

    private final String deviceId;

    private final String browser;

    private final String operatingSystem;

    private final String deviceName;

    private final String ipAddress;

    private String deviceClass;
}