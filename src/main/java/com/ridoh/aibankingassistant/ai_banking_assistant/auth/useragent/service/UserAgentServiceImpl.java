package com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.dto.UserAgentInfo;
import jakarta.servlet.http.HttpServletRequest;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;

@Service
public class UserAgentServiceImpl implements UserAgentService {

    private final UserAgentAnalyzer analyzer =
            UserAgentAnalyzer.newBuilder()
                    .hideMatcherLoadStats()
                    .withCache(1000)
                    .build();

    @Override
    public UserAgentInfo parse(HttpServletRequest request) {

        String header = request.getHeader("User-Agent");

        if (header == null) {
            header = "";
        }

        UserAgent userAgent = analyzer.parse(header);

        String browser =
                userAgent.getValue(UserAgent.AGENT_NAME);

        String operatingSystem =
                userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME);

        String deviceClass =
                userAgent.getValue(UserAgent.DEVICE_CLASS);

        String deviceName = buildDeviceName(
                deviceClass,
                operatingSystem,
                browser
        );

        return UserAgentInfo.builder()
                .browser(browser)
                .operatingSystem(operatingSystem)
                .deviceClass(deviceClass)
                .deviceName(deviceName)
                .build();
    }

    private String buildDeviceName(
            String deviceClass,
            String operatingSystem,
            String browser
    ) {

        return switch (deviceClass) {

            case "Desktop" ->
                    "Desktop • " + operatingSystem + " • " + browser;

            case "Phone" ->
                    "Phone • " + operatingSystem + " • " + browser;

            case "Tablet" ->
                    "Tablet • " + operatingSystem + " • " + browser;

            case "Watch" ->
                    "Watch • " + operatingSystem + " • " + browser;

            case "TV" ->
                    "TV • " + operatingSystem + " • " + browser;

            default ->
                    operatingSystem + " • " + browser;
        };
    }
}