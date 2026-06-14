package com.ridoh.aibankingassistant.ai_banking_assistant.account.dto;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.AccountStatus;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private AccountStatus status;
    private BigDecimal balance;
    private Long userId;
    private LocalDateTime createdAt;
}