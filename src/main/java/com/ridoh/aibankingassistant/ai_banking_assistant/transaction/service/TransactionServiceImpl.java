package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.Account;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.AccountStatus;
import com.ridoh.aibankingassistant.ai_banking_assistant.account.repository.AccountRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.*;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private static final String REFERENCE_PREFIX = "TXN";
    private static final int REFERENCE_RANDOM_BOUND = 1_000_000;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountAuthorizationService accountAuthorizationService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public TransactionResponse deposit(CreateTransactionRequest request) {
        Account account = findAccountByNumber(request.getAccountNumber());
        accountAuthorizationService.validateAccountOwnership(account);
        validateDepositAllowed(account);
        account.setBalance(account.getBalance().add(request.getAmount()));

        Transaction transaction = createTransaction(account, request, TransactionType.DEPOSIT);
        accountRepository.save(account);

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(CreateTransactionRequest request) {
        Account account = findAccountByNumber(request.getAccountNumber());
        accountAuthorizationService.validateAccountOwnership(account);
        validateWithdrawalAllowed(account);
        validateSufficientBalance(account.getBalance(), request.getAmount());
        account.setBalance(account.getBalance().subtract(request.getAmount()));

        Transaction transaction = createTransaction(account, request, TransactionType.WITHDRAWAL);
        accountRepository.save(account);

        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
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
                buildTransferDescription(
                        "Transfer to "
                                + destinationAccount.getUser().getFullName()
                                + " ("
                                + destinationAccount.getAccountNumber()
                                + ")",
                        request.getNarration())
        );
        Transaction creditTransaction = createTransaction(
                destinationAccount,
                request.getAmount(),
                TransactionType.DEPOSIT,
                buildTransferDescription(
                        "Transfer from "
                                + sourceAccount.getUser().getFullName()
                                + " ("
                                + sourceAccount.getAccountNumber()
                                + ")",
                        request.getNarration())
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction savedDebitTransaction = transactionRepository.save(debitTransaction);
        Transaction savedCreditTransaction = transactionRepository.save(creditTransaction);

        return TransferResponse.builder()
                .transferReference(transferReference)
                .sourceAccountNumber(sourceAccount.getAccountNumber())
                .destinationAccountNumber(destinationAccount.getAccountNumber())
                .amount(request.getAmount())
                .debitTransaction(toResponse(savedDebitTransaction))
                .creditTransaction(toResponse(savedCreditTransaction))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(String accountNumber) {
        Account account = findAccountByNumber(accountNumber);
        accountAuthorizationService.validateAccountOwnership(account);

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
