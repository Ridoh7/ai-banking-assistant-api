package com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAgentInfo {

    private String browser;

    private String operatingSystem;

    private String deviceClass;

    private String deviceName;
}