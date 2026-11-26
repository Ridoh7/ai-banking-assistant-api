package com.ridoh.aibankingassistant.ai_banking_assistant.audit.repository;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Persistence repository for immutable enterprise audit records.
 *
 * The repository supports:
 *
 * - Append-only audit persistence
 * - Retrieval by audit identifier
 * - Pagination and sorting
 * - Dynamic filtering through JPA Specifications
 *
 * Audit records must never be updated or deleted by application services.
 *
 * Although JpaRepository technically exposes mutation methods, audit
 * immutability is protected by:
 *
 * 1. The absence of update and delete operations in the audit service
 * 2. Hibernate's @Immutable annotation on AuditLog
 * 3. Non-updatable entity columns
 * 4. The PostgreSQL trigger rejecting UPDATE and DELETE operations
 *
 * This repository must remain internal to the audit module and must not
 * be injected directly into controllers or unrelated domain services.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>,
        JpaSpecificationExecutor<AuditLog> {
}