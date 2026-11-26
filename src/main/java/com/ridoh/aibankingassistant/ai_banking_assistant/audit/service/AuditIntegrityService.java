package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditIntegrityPayload;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import jakarta.validation.constraints.NotNull;

/**
 * Generates and verifies tamper-evident SHA-256 integrity hashes
 * for immutable audit records.
 */
public interface AuditIntegrityService {

    /**
     * Generates a deterministic SHA-256 hash from canonical immutable
     * audit-event fields.
     *
     * Identical values must produce the same hash regardless of JSON
     * object-property insertion order.
     *
     * @param payload finalized immutable audit values
     * @return lowercase 64-character SHA-256 hexadecimal hash
     */
    String generateHash(
            @NotNull
            AuditIntegrityPayload payload
    );

    /**
     * Recomputes the integrity hash from the persisted audit record.
     *
     * @param auditLog persisted audit record
     * @return true when the stored and calculated hashes match
     */
    boolean verify(
            @NotNull
            AuditLog auditLog
    );
}