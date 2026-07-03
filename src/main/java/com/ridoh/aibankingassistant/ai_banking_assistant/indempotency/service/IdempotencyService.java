package com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;

import java.util.Optional;

public interface IdempotencyService {

    <T> Optional<T> findStoredResponse(
            String idempotencyKey,
            String endpoint,
            Object request,
            Class<T> responseType
    );

    void saveSuccessfulResponse(
            String idempotencyKey,
            String endpoint,
            Object request,
            ApiResponse<?> response
    );
}
