package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditEventCommand;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditResourceReference;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditLogService;
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
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

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
        User currentUser = getCurrentUser();
        UUID currentSessionId = SecurityUtils.getCurrentSessionId();

        RefreshToken refreshToken;
        try {
            refreshToken = refreshTokenRepository
                    .findBySessionIdAndRevokedFalse(sessionId)
                    .orElseThrow(() ->
                            new ForbiddenException("Session not found."));

            if (!refreshToken.getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException(
                        "You are not allowed to revoke this session."
                );
            }
        } catch (ForbiddenException exception) {
            recordSessionOwnershipFailure(
                    sessionId,
                    currentUser,
                    exception
            );
            throw exception;
        }

        int revokedSessions =
                refreshTokenRepository.revokeSession(sessionId);

        AuditEventType eventType =
                sessionId.equals(currentSessionId)
                        ? AuditEventType.CURRENT_SESSION_LOGOUT
                        : AuditEventType.SPECIFIC_SESSION_LOGOUT;

        auditLogService.record(
                buildSessionSuccessEvent(
                        eventType,
                        sessionId.toString(),
                        currentSessionId,
                        currentUser,
                        revokedSessions,
                        sessionId.equals(currentSessionId)
                                ? "Current session logged out"
                                : "Specific session logged out"
                )
        );
    }

    @Override
    @Transactional
    public void logoutOtherSessions() {

        User currentUser = getCurrentUser();

        UUID currentSessionId = SecurityUtils.getCurrentSessionId();

        int revokedSessions =
                refreshTokenRepository.revokeOtherSessions(
                        currentUser,
                        currentSessionId
                );

        auditLogService.record(
                buildSessionSuccessEvent(
                        AuditEventType.OTHER_SESSIONS_LOGOUT,
                        "OTHER_SESSIONS",
                        currentSessionId,
                        currentUser,
                        revokedSessions,
                        "All other sessions logged out"
                )
        );
    }

    @Override
    @Transactional
    public void logoutAllSessions() {

        User currentUser = getCurrentUser();
        UUID currentSessionId = SecurityUtils.getCurrentSessionId();

        int revokedSessions =
                refreshTokenRepository.revokeAllSessions(currentUser);

        auditLogService.record(
                buildSessionSuccessEvent(
                        AuditEventType.ALL_SESSIONS_LOGOUT,
                        "ALL_SESSIONS",
                        currentSessionId,
                        currentUser,
                        revokedSessions,
                        "All user sessions logged out"
                )
        );
    }

    private AuditEventCommand buildSessionSuccessEvent(
            AuditEventType eventType,
            String resourceId,
            UUID initiatingSessionId,
            User user,
            int revokedSessions,
            String description
    ) {
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("revokedSessionCount", revokedSessions);

        return AuditEventCommand.builder()
                .eventType(eventType)
                .primaryResource(AuditResourceReference.of(
                        AuditResourceType.SESSION,
                        resourceId
                ))
                .secondaryResource(AuditResourceReference.of(
                        AuditResourceType.USER,
                        user.getId().toString()
                ))
                .sessionId(initiatingSessionId)
                .description(description)
                .metadata(metadata)
                .build();
    }

    private void recordSessionOwnershipFailure(
            UUID requestedSessionId,
            User currentUser,
            ForbiddenException exception
    ) {
        auditLogService.recordFailure(
                AuditEventCommand.builder()
                        .eventType(
                                AuditEventType
                                        .SESSION_OWNERSHIP_VALIDATION_FAILURE
                        )
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.SESSION,
                                requestedSessionId
                        ))
                        .secondaryResource(AuditResourceReference.of(
                                AuditResourceType.USER,
                                currentUser.getId().toString()
                        ))
                        .description(
                                "Session ownership validation failed"
                        )
                        .failureCode(
                                exception.getClass().getSimpleName()
                        )
                        .failureReason(exception.getMessage())
                        .build()
        );
    }
}