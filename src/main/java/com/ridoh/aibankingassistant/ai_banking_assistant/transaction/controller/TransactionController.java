package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.CreateTransactionRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransactionResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.service.TransactionService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse response = transactionService.deposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deposit completed successfully", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        TransactionResponse response = transactionService.withdraw(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Withdrawal completed successfully", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @Valid @RequestBody TransferRequest request
    ) {
        TransferResponse response = transactionService.transfer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transfer completed successfully", response));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @PathVariable String accountNumber
    ) {
        List<TransactionResponse> response = transactionService.getTransactions(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }
}