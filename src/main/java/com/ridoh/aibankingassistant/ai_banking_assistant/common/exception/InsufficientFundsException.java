package com.ridoh.aibankingassistant.ai_banking_assistant.common.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}