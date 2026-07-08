package com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.auth.useragent.dto.UserAgentInfo;
import jakarta.servlet.http.HttpServletRequest;

public interface UserAgentService {

    UserAgentInfo parse(HttpServletRequest request);

}