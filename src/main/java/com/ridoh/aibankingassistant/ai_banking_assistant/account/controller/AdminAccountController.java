package com.ridoh.aibankingassistant.ai_banking_assistant.account.controller;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.service.AccountService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
        List<AccountResponse> response = accountService.getAllAccounts();
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved successfully", response));
    }

    @PutMapping("/{accountNumber}/freeze")
    public ResponseEntity<ApiResponse<AccountResponse>> freezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.freezeAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account frozen successfully", response));
    }

    @PutMapping("/{accountNumber}/unfreeze")
    public ResponseEntity<ApiResponse<AccountResponse>> unfreezeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.unfreezeAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account unfrozen successfully", response));
    }

    @PutMapping("/{accountNumber}/close")
    public ResponseEntity<ApiResponse<AccountResponse>> closeAccount(@PathVariable String accountNumber) {
        AccountResponse response = accountService.closeAccount(accountNumber);
        return ResponseEntity.ok(ApiResponse.success("Account closed successfully", response));
    }
}