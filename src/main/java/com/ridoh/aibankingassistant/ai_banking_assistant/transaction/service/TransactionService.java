package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.CreateTransactionRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransactionResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferResponse;

import java.util.List;

public interface TransactionService {

    TransactionResponse deposit(CreateTransactionRequest request);

    TransactionResponse withdraw(CreateTransactionRequest request);

    List<TransactionResponse> getTransactions(String accountNumber);

    TransferResponse transfer(TransferRequest request);
}