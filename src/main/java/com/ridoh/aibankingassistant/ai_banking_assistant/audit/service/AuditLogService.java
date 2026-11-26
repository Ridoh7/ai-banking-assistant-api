package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditEventCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Internal write contract for creating immutable enterprise audit records.
 *
 * This service is used by authentication, session, account, transaction,
 * administration, and security components.
 *
 * It intentionally exposes no update or delete operations.
 */
public interface AuditLogService {

    /**
     * Records an audit event using the current transactional boundary.
     *
     * This method is intended for successful critical operations where
     * the business state change and audit record must commit together.
     *
     * Examples:
     *
     * - Successful deposit
     * - Successful withdrawal
     * - Successful transfer
     * - Account freeze
     * - Account unfreeze
     * - Account closure
     * - Refresh-token rotation
     *
     * When audit persistence fails, the surrounding business transaction
     * should roll back.
     *
     * @param command structured and sanitized audit command
     * @return identifier of the persisted audit record
     */
    UUID record(
            @NotNull
            @Valid
            AuditEventCommand command
    );

    /**
     * Records an unsuccessful audit event in an independent transaction.
     *
     * This method is intended for FAILURE and DENIED events where the
     * original business transaction may already be marked for rollback.
     *
     * Examples:
     *
     * - Insufficient balance
     * - Ownership validation failure
     * - Invalid refresh token
     * - Revoked-session access
     * - Authorization denial
     * - Idempotency conflict
     *
     * The implementation will use REQUIRES_NEW transaction propagation.
     *
     * @param command structured and sanitized unsuccessful audit command
     * @return identifier of the persisted audit record
     */
    UUID recordFailure(
            @NotNull
            @Valid
            AuditEventCommand command
    );

    /**
     * Records a system-generated audit event.
     *
     * This method is intended for operations that do not originate from
     * an authenticated HTTP request.
     *
     * The implementation will supply:
     *
     * - SYSTEM actor information
     * - Generated correlation ID
     * - Generated request ID
     * - Null session and HTTP context where unavailable
     *
     * @param command system audit command
     * @return identifier of the persisted audit record
     */
    UUID recordSystemEvent(
            @NotNull
            @Valid
            AuditEventCommand command
    );
}