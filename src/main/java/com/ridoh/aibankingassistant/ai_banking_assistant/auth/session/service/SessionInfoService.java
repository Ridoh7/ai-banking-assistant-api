package com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionInfo;
import com.ridoh.aibankingassistant.ai_banking_assistant.auth.session.dto.SessionResponse;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SessionInfoService {

    SessionInfo getCurrentSession();

}