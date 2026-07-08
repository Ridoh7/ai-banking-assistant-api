package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity.RefreshToken;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.repository.RefreshTokenRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ForbiddenException;
import com.ridoh.aibankingassistant.ai_banking_assistant.security.SecurityUtils;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionManagementServiceImpl implements SessionManagementService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {

        String email = SecurityUtils.getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SessionResponse> getCurrentUserSessions(Pageable pageable) {

        User currentUser = getCurrentUser();

        UUID currentSessionId = SecurityUtils.getCurrentSessionId();

        Page<RefreshToken> sessions =
                refreshTokenRepository.findByUserAndRevokedFalse(
                        currentUser,
                        pageable
                );

        return sessions.map(refreshToken ->
                mapToSessionResponse(
                        refreshToken,
                        currentSessionId
                )
        );
    }

    private SessionResponse mapToSessionResponse(
            RefreshToken refreshToken,
            UUID currentSessionId
    ) {

        return SessionResponse.builder()
                .sessionId(refreshToken.getSessionId())
                .deviceClass(refreshToken.getDeviceClass())
                .deviceName(refreshToken.getDeviceName())
                .browser(refreshToken.getBrowser())
                .operatingSystem(refreshToken.getOperatingSystem())
                .ipAddress(refreshToken.getIpAddress())
                .createdAt(refreshToken.getCreatedAt())
                .lastUsedAt(refreshToken.getLastUsedAt())
                .expiresAt(refreshToken.getExpiresAt())
                .revoked(refreshToken.isRevoked())
                .currentSession(
                        refreshToken.getSessionId().equals(currentSessionId)
                )
                .build();
    }

    @Override
    @Transactional
    public void logoutSession(UUID sessionId) {

        RefreshToken refreshToken = refreshTokenRepository
                .findBySessionIdAndRevokedFalse(sessionId)
                .orElseThrow(() ->
                        new ForbiddenException("Session not found."));

        User currentUser = getCurrentUser();

        if (!refreshToken.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not allowed to revoke this session.");
        }

        refreshTokenRepository.revokeSession(sessionId);
    }

    @Override
    @Transactional
    public void logoutOtherSessions() {

        User currentUser = getCurrentUser();

        UUID currentSessionId = SecurityUtils.getCurrentSessionId();

        refreshTokenRepository.revokeOtherSessions(
                currentUser,
                currentSessionId
        );
    }

    @Override
    @Transactional
    public void logoutAllSessions() {

        User currentUser = getCurrentUser();

        refreshTokenRepository.revokeAllSessions(currentUser);
    }
}