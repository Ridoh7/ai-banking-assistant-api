package com.ridoh.aibankingassistant.ai_banking_assistant.common.exception;

public class AccountClosedException extends RuntimeException {

    public AccountClosedException(String message) {
        super(message);
    }
}