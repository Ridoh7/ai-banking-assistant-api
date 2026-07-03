package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.IdempotentResult;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.CreateTransactionRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransactionResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferResponse;
import java.util.List;

public interface TransactionService {

    IdempotentResult<TransactionResponse> deposit(
            CreateTransactionRequest request,
            String idempotencyKey
    );

    IdempotentResult<TransactionResponse> withdraw(
            CreateTransactionRequest request,
            String idempotencyKey
    );

    IdempotentResult<TransferResponse> transfer(
            TransferRequest request,
            String idempotencyKey
    );

    List<TransactionResponse> getTransactions(String accountNumber);
}