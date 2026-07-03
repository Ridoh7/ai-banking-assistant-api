package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.*;

public interface AuthService {

    AuthResponse registerUser(RegisterRequest request);

    AuthResponse registerAdmin(AdminRegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);
}