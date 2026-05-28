package com.ridoh.aibankingassistant.ai_banking_assistant.auth.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.AdminRegisterRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.AuthResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.LoginRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse registerUser(RegisterRequest request);

    AuthResponse registerAdmin(AdminRegisterRequest request);

    AuthResponse login(LoginRequest request);
}