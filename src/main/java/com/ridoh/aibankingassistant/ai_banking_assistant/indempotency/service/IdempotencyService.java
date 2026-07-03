package com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.service;

public class in {

}

public interface IdempotencyService {

    boolean exists(String idempotencyKey);
    Optional<ApiResponse<?>> findStoredResponse(String idempotencyKey, String endpoint, Object request);

    void saveSuccessfulResponse(
            String idempotencyKey,
            String endpoint,
            Object request,
            ApiResponse<?> response
}
