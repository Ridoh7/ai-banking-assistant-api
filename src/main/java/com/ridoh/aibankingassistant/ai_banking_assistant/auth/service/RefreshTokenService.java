package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.entity.RefreshToken;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeRefreshToken(RefreshToken refreshToken);

    void revokeAllUserTokens(User user);
}