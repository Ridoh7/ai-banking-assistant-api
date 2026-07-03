package com.ridoh.aibankingassistant.ai_banking_assistant.common.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdempotentResult<T> {

    private final T response;

    /**
     * true = returned from idempotency storage
     * false = newly processed
     */
    private final boolean replayed;
}