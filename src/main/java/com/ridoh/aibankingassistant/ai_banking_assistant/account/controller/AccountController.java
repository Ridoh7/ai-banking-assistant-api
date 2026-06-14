package com.ridoh.aibankingassistant.ai_banking_assistant.account.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountBalanceResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountValidationResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.CreateAccountRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.service.AccountService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        AccountResponse response = accountService.createAccount(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getOwnAccounts() {
        List<AccountResponse> response = accountService.getOwnAccounts();
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", response));
    }


    @GetMapping("/validate/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountValidationResponse>> validateAccount(
            @PathVariable String accountNumber
    ) {
        AccountValidationResponse response = accountService.validateAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account validated successfully", response));
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<ApiResponse<AccountBalanceResponse>> getAccountBalance(
            @PathVariable String accountNumber
    ) {
        AccountBalanceResponse response = accountService.getAccountBalance(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Balance retrieved successfully", response));
    }
}