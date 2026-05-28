package com.ridoh.aibankingassistant.ai_banking_assistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.admin")
public record AdminProperties(String secret) {
}