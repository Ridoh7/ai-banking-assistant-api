package com.ridoh.aibankingassistant.ai_banking_assistant.user.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.user.dto.UserResponse;
import java.util.List;

public interface UserService {

    List<UserResponse> getAllUsers();
}