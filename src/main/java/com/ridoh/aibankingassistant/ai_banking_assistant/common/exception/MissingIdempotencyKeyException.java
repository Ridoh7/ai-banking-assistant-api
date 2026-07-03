package com.ridoh.aibankingassistant.ai_banking_assistant.common.exception;

public class MissingIdempotencyKeyException extends RuntimeException {

    public MissingIdempotencyKeyException(String message) {
        super(message);
    }
}