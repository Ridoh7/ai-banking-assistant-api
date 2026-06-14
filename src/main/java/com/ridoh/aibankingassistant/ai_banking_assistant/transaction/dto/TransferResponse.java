package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {

    private String transferReference;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private TransactionResponse debitTransaction;
    private TransactionResponse creditTransaction;
}