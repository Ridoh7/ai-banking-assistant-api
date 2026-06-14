package com.ridoh.aibankingassistant.ai_banking_assistant.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResponse {

    private String accountNumber;
    private String accountName;
}