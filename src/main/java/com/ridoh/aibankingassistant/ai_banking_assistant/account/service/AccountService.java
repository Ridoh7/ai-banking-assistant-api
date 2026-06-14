package com.ridoh.aibankingassistant.ai_banking_assistant.account.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountBalanceResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountValidationResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.CreateAccountRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import java.util.List;

public interface AccountService {

    AccountResponse createAccount(CreateAccountRequest request, User currentUser);

    List<AccountResponse> getOwnAccounts();

    List<AccountResponse> getAllAccounts();

    AccountValidationResponse validateAccount(String accountNumber);

    AccountBalanceResponse getAccountBalance(String accountNumber);

    AccountResponse freezeAccount(String accountNumber);

    AccountResponse unfreezeAccount(String accountNumber);

    AccountResponse closeAccount(String accountNumber);
}