package com.ridoh.aibankingassistant.ai_banking_assistant;

import com.ridoh.aibankingassistant.ai_banking_assistant.config.AdminProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AdminProperties.class)
public class AiBankingAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiBankingAssistantApplication.class, args);
    }
}
