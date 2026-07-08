package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity.RefreshToken;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionInfo;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;

import java.util.UUID;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, SessionInfo sessionInfo);

    RefreshToken createRefreshToken(User user, SessionInfo sessionInfo, UUID sessionId);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeRefreshToken(RefreshToken refreshToken);

    void revokeAllUserTokens(User user);
}