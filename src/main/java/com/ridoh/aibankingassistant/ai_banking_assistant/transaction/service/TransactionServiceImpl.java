package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.Account;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.AccountStatus;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.repository.AccountRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.ApiResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.dto.IdempotentResult;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.AccountClosedException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.AccountFrozenException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.BadRequestException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.InsufficientFundsException;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ResourceNotFoundException;
import com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.service.IdempotencyService;
import com.ridoh.aibankingassistant.ai_banking_assistant.security.AccountAuthorizationService;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.CreateTransactionRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransactionResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.dto.TransferResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.entity.Transaction;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.entity.TransactionType;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.repository.TransactionRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final String REFERENCE_PREFIX = "TXN";
    private static final int REFERENCE_RANDOM_BOUND = 1_000_000;
    private static final String DEPOSIT_ENDPOINT = "/api/v1/transactions/deposit";
    private static final String WITHDRAW_ENDPOINT = "/api/v1/transactions/withdraw";
    private static final String TRANSFER_ENDPOINT = "/api/v1/transactions/transfer";
    private static final String DEPOSIT_SUCCESS_MESSAGE = "Deposit completed successfully";
    private static final String WITHDRAWAL_SUCCESS_MESSAGE = "Withdrawal completed successfully";
    private static final String TRANSFER_SUCCESS_MESSAGE = "Transfer completed successfully";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountAuthorizationService accountAuthorizationService;
    private final IdempotencyService idempotencyService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public IdempotentResult<TransactionResponse> deposit(CreateTransactionRequest request, String idempotencyKey) {

        Optional<TransactionResponse> stored =
                idempotencyService.findStoredResponse(
                        idempotencyKey,
                        DEPOSIT_ENDPOINT,
                        request,
                        TransactionResponse.class
                );

        if (stored.isPresent()) {
            return IdempotentResult.<TransactionResponse>builder()
                    .response(stored.get())
                    .replayed(true)
                    .build();
        }

        TransactionResponse response = processDeposit(request, idempotencyKey);

        return IdempotentResult.<TransactionResponse>builder()
                .response(response)
                .replayed(false)
                .build();
    }

    private TransactionResponse processDeposit(CreateTransactionRequest request, String idempotencyKey) {
        log.info("Executing deposit business logic");
        Account account = findAccountByNumber(request.getAccountNumber());
        accountAuthorizationService.validateAccountOwnership(account);
        validateDepositAllowed(account);
        account.setBalance(account.getBalance().add(request.getAmount()));

        Transaction transaction = createTransaction(account, request, TransactionType.DEPOSIT);
        accountRepository.save(account);

        TransactionResponse response = toResponse(transactionRepository.save(transaction));
        idempotencyService.saveSuccessfulResponse(
                idempotencyKey,
                DEPOSIT_ENDPOINT,
                request,
                ApiResponse.success(DEPOSIT_SUCCESS_MESSAGE, response)
        );

        return response;
    }

    @Override
    @Transactional
    public IdempotentResult<TransactionResponse> withdraw(CreateTransactionRequest request, String idempotencyKey) {
        Optional<TransactionResponse> stored =
                idempotencyService.findStoredResponse(
                        idempotencyKey,
                        WITHDRAW_ENDPOINT,
                        request,
                        TransactionResponse.class
                );

        if (stored.isPresent()) {
            return IdempotentResult.<TransactionResponse>builder()
                    .response(stored.get())
                    .replayed(true)
                    .build();
        }

        TransactionResponse response = processWithdrawal(request, idempotencyKey);

        return IdempotentResult.<TransactionResponse>builder()
                .response(response)
                .replayed(false)
                .build();
    }

    private TransactionResponse processWithdrawal(CreateTransactionRequest request, String idempotencyKey) {
        log.info("Executing withdrawal business logic");
        Account account = findAccountByNumber(request.getAccountNumber());
        accountAuthorizationService.validateAccountOwnership(account);
        validateWithdrawalAllowed(account);
        validateSufficientBalance(account.getBalance(), request.getAmount());
        account.setBalance(account.getBalance().subtract(request.getAmount()));

        Transaction transaction = createTransaction(account, request, TransactionType.WITHDRAWAL);
        accountRepository.save(account);

        TransactionResponse response = toResponse(transactionRepository.save(transaction));
        idempotencyService.saveSuccessfulResponse(
                idempotencyKey,
                WITHDRAW_ENDPOINT,
                request,
                ApiResponse.success(WITHDRAWAL_SUCCESS_MESSAGE, response)
        );

        return response;
    }

    @Override
    @Transactional
    public IdempotentResult<TransferResponse> transfer(TransferRequest request, String idempotencyKey) {
        Optional<TransferResponse> stored =
                idempotencyService.findStoredResponse(
                        idempotencyKey,
                        TRANSFER_ENDPOINT,
                        request,
                        TransferResponse.class
                );

        if (stored.isPresent()) {
            return IdempotentResult.<TransferResponse>builder()
                    .response(stored.get())
                    .replayed(true)
                    .build();
        }

        TransferResponse response = processTransfer(request, idempotencyKey);

        return IdempotentResult.<TransferResponse>builder()
                .response(response)
                .replayed(false)
                .build();
    }

    private TransferResponse processTransfer(TransferRequest request, String idempotencyKey) {
        Account sourceAccount = findAccountByNumber(request.getSourceAccountNumber());
        Account destinationAccount = findAccountByNumber(request.getDestinationAccountNumber());

        accountAuthorizationService.validateAccountOwnership(sourceAccount);
        validateDifferentAccounts(sourceAccount, destinationAccount);
        validateTransferAllowed(sourceAccount, destinationAccount);
        validateSufficientBalance(sourceAccount.getBalance(), request.getAmount());

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        destinationAccount.setBalance(destinationAccount.getBalance().add(request.getAmount()));

        String transferReference = generateTransferReference();
        Transaction debitTransaction = createTransaction(
                sourceAccount,
                request.getAmount(),
                TransactionType.WITHDRAWAL,
                buildTransferDescription("Transfer to " + destinationAccount.getAccountNumber(), request.getNarration())
        );
        Transaction creditTransaction = createTransaction(
                destinationAccount,
                request.getAmount(),
                TransactionType.DEPOSIT,
                buildTransferDescription("Transfer from " + sourceAccount.getAccountNumber(), request.getNarration())
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction savedDebitTransaction = transactionRepository.save(debitTransaction);
        Transaction savedCreditTransaction = transactionRepository.save(creditTransaction);

        TransferResponse response = TransferResponse.builder()
                .transferReference(transferReference)
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(request.getAmount())
                .debitTransaction(toResponse(savedDebitTransaction))
                .creditTransaction(toResponse(savedCreditTransaction))
                .build();

        idempotencyService.saveSuccessfulResponse(
                idempotencyKey,
                TRANSFER_ENDPOINT,
                request,
                ApiResponse.success(TRANSFER_SUCCESS_MESSAGE, response)
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        accountAuthorizationService.validateAccountAccess(account);

        return transactionRepository.findByAccount(account)
                .stream()
                .sorted(Comparator.comparing(Transaction::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private void validateSufficientBalance(BigDecimal balance, BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }

    private void validateDifferentAccounts(Account sourceAccount, Account destinationAccount) {
        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new BadRequestException("Source and destination accounts cannot be the same");
        }
    }

    private void validateDepositAllowed(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot receive deposits");
        }
    }

    private void validateWithdrawalAllowed(Account account) {
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed account cannot be debited");
        }
        if (account.getStatus() == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Frozen account cannot be debited");
        }
    }

    private void validateTransferAllowed(Account sourceAccount, Account destinationAccount) {
        validateWithdrawalAllowed(sourceAccount);
        if (destinationAccount.getStatus() == AccountStatus.CLOSED) {
            throw new AccountClosedException("Closed destination account cannot receive transfers");
        }
    }

    private Transaction createTransaction(
            Account account,
            CreateTransactionRequest request,
            TransactionType transactionType
    ) {
        return createTransaction(account, request.getAmount(), transactionType, request.getDescription());
    }

    private Transaction createTransaction(
            Account account,
            BigDecimal amount,
            TransactionType transactionType,
            String description
    ) {
        return Transaction.builder()
                .reference(generateUniqueReference())
                .amount(amount)
                .transactionType(transactionType)
                .description(description)
                .account(account)
                .build();
    }

    private String buildTransferDescription(String defaultDescription, String narration) {
        if (narration == null || narration.isBlank()) {
            return defaultDescription;
        }

        return defaultDescription + " - " + narration;
    }

    private String generateTransferReference() {
        return "TRF" + System.currentTimeMillis() + secureRandom.nextInt(REFERENCE_RANDOM_BOUND);
    }

    private String generateUniqueReference() {
        String reference;
        do {
            reference = REFERENCE_PREFIX
                    + System.currentTimeMillis()
                    + secureRandom.nextInt(REFERENCE_RANDOM_BOUND);
        } while (transactionRepository.existsByReference(reference));

        return reference;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .reference(transaction.getReference())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .description(transaction.getDescription())
                .accountNumber(transaction.getAccount().getAccountNumber())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}