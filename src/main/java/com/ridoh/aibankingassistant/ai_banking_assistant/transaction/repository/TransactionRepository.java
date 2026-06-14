package com.ridoh.aibankingassistant.ai_banking_assistant.transaction.repository;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.Account;
import com.ridoh.aibankingassistant.ai_banking_assistant.transaction.entity.Transaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccount(Account account);

    boolean existsByReference(String reference);
}