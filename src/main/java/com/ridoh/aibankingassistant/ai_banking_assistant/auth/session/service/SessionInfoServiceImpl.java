package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.repository.RefreshTokenRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionInfo;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.dto.UserAgentInfo;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.service.UserAgentService;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class SessionInfoServiceImpl implements SessionInfoService {

    private final UserAgentService userAgentService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public SessionInfo getCurrentSession() {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return SessionInfo.builder()
                    .deviceId(UUID.randomUUID().toString())
                    .browser("Unknown")
                    .operatingSystem("Unknown")
                    .deviceName("Unknown Device")
                    .ipAddress("Unknown")
                    .build();
        }

        HttpServletRequest request = attributes.getRequest();

        UserAgentInfo userAgentInfo = userAgentService.parse(request);

        return SessionInfo.builder()
                .deviceId(generateDeviceId())
                .deviceName(userAgentInfo.getDeviceName())
                .browser(userAgentInfo.getBrowser())
                .operatingSystem(userAgentInfo.getOperatingSystem())
                .deviceClass(userAgentInfo.getDeviceClass())
                .ipAddress(resolveIpAddress(request))
                .build();
    }

    private String generateDeviceId() {
        return UUID.randomUUID().toString();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}