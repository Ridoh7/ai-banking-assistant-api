package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.IdempotentResult;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.MissingIdempotencyKeyException;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private final TransactionService transactionService;

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        IdempotentResult<TransactionResponse> result =
                transactionService.deposit(request, requireIdempotencyKey(idempotencyKey));

        HttpStatus status = result.isReplayed()
                ? HttpStatus.OK
                : HttpStatus.CREATED;

        return ResponseEntity.status(status)
                .body(ApiResponse.success(
                        "Deposit completed successfully",
                        result.getResponse()
                ));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        IdempotentResult<TransactionResponse> result =
                transactionService.withdraw(request, requireIdempotencyKey(idempotencyKey));

        HttpStatus status = result.isReplayed()
                ? HttpStatus.OK
                : HttpStatus.CREATED;

        return ResponseEntity.status(status)
                .body(ApiResponse.success(
                        "Withdrawal completed successfully",
                        result.getResponse()
                ));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransferResponse>> transfer(
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest request
    ) {
        IdempotentResult<TransferResponse> result =
                transactionService.transfer(request, requireIdempotencyKey(idempotencyKey));

        HttpStatus status = result.isReplayed()
                ? HttpStatus.OK
                : HttpStatus.CREATED;

        return ResponseEntity.status(status)
                .body(ApiResponse.success(
                        "Transfer completed successfully",
                        result.getResponse()
                ));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactions(
            @PathVariable String accountNumber
    ) {
        List<TransactionResponse> response = transactionService.getTransactions(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved successfully", response));
    }

    private String requireIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new MissingIdempotencyKeyException("Idempotency-Key header is required");
        }

        return idempotencyKey;
    }
}