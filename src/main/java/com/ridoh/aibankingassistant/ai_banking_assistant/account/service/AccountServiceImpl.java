package com.ridoh.aibankingassistant.ai_banking_assistant.account.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountBalanceResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.AccountValidationResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.dto.CreateAccountRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.Account;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.AccountStatus;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.repository.AccountRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.AccountClosedException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.InvalidAccountStatusException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ResourceNotFoundException;
import com.ridoh.aibankingassistant.ai_banking_assistant.security.AccountAuthorizationService;
import com.ridoh.aibankingassistant.ai_banking_assistant.security.CurrentUserService;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final int ACCOUNT_NUMBER_LENGTH = 10;

    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;
    private final AccountAuthorizationService accountAuthorizationService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request, User currentUser) {
        Account account = Account.builder()
                .accountNumber(generateUniqueAccountNumber())
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance())
                .user(currentUser)
                .build();

        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getOwnAccounts() {
        User currentUser = currentUserService.getCurrentUser();
        return accountRepository.findByUser(currentUser)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountValidationResponse validateAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        accountAuthorizationService.validateAccountAccess(account);

        return AccountValidationResponse.builder()
                .accountNumber(account.getAccountNumber())
                .accountName(account.getUser().getFullName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceResponse getAccountBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        accountAuthorizationService.validateAccountAccess(account);

        return AccountBalanceResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .build();
    }

    @Override
    @Transactional
    public AccountResponse freezeAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot be frozen");
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new InvalidAccountStatusException("Account is already frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse unfreezeAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot be reopened");
        }
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new InvalidAccountStatusException("Only frozen accounts can be unfrozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        return toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse closeAccount(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new InvalidAccountStatusException("Account is already closed");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new InvalidAccountStatusException("Account balance must be zero before closure");
        }

        account.setStatus(AccountStatus.CLOSED);
        return toResponse(accountRepository.save(account));
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = secureRandom.ints(ACCOUNT_NUMBER_LENGTH, 0, 10)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining());
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    private AccountResponse toResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .userId(account.getUser().getId())
                .createdAt(account.getCreatedAt())
                .build();
    }
}