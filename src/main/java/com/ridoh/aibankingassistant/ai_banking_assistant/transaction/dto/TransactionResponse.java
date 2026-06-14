package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto;

import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.entity.TransactionType;
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
public class TransactionResponse {

    private Long id;
    private String reference;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String description;
    private String accountNumber;
    private LocalDateTime createdAt;
}