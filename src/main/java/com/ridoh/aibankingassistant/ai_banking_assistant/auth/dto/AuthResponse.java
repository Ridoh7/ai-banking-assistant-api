package com.ridoh.aibankingassistant.ai_banking_assistant.auth.dto;

import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String fullName;
    private String email;
    private Role role;
}
