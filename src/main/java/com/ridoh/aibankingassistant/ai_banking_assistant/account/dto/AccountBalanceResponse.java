package com.ridoh.aibankingassistant.ai_banking_assistant.account.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceResponse {

    private String accountNumber;
    private BigDecimal balance;
}