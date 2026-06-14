package com.ridoh.aibankingassistant.ai_banking_assistant.common.exception;

public class AccountFrozenException extends RuntimeException {

    public AccountFrozenException(String message) {
        super(message);
    }
}