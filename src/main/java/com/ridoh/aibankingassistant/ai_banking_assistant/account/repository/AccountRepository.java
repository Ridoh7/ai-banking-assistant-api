package com.ridoh.aibankingassistant.ai_banking_assistant.account.repository;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.Account;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByAccountNumber(String accountNumber);

    List<Account> findByUser(User user);

    Optional<Account> findByAccountNumber(String accountNumber);

}