package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionResponse;
import java.util.UUID;

import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface SessionManagementService {

    Page<SessionResponse> getCurrentUserSessions(Pageable pageable);

    void logoutSession(UUID sessionId);

    void logoutOtherSessions();

    void logoutAllSessions();
}