package com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.repository;

import  com.ridoh.aibankingassistant.ai_banking_assistant.indempotency.entity.IdempotencyKey;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}