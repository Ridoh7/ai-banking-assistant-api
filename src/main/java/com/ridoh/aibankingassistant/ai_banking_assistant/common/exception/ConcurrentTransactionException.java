package com.ridoh.aibankingassistant.ai_banking_assistant.common.exception;

public class ConcurrentTransactionException extends RuntimeException {

    public ConcurrentTransactionException(String message) {
        super(message);
    }
}