package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

/**
 * Internal command used by banking, authentication, session, security,
 * and administrative services to request creation of an audit record.
 *
 * This command is never accepted directly from a public API endpoint.
 *
 * The audit service will:
 *
 * 1. Validate the command
 * 2. Resolve missing actor information
 * 3. Enrich request and device context
 * 4. Sanitize event fields
 * 5. Generate identifiers and timestamps
 * 6. Generate the integrity hash
 * 7. Persist the immutable audit record
 */
@Builder
public record AuditEventCommand(

        @NotNull
        AuditEventType eventType,

        @Valid
        AuditActorSnapshot actor,

        @Valid
        AuditResourceReference primaryResource,

        @Valid
        AuditResourceReference secondaryResource,

        @Size(max = 1000)
        String description,

        @Size(max = 100)
        String failureCode,

        @Size(max = 500)
        String failureReason,

        JsonNode metadata,

        JsonNode previousState,

        JsonNode newState,

        /**
         * Optional correlation ID.
         *
         * When absent, the audit context provider will use the current
         * request correlation ID or generate a new one.
         */
        UUID correlationId,

        /**
         * Optional request ID override.
         *
         * Normally populated by the audit context provider.
         */
        UUID requestId,

        /**
         * Optional session ID override.
         *
         * Normally resolved from the authenticated JWT/session context.
         */
        UUID sessionId
) {

    /**
     * Returns true when the command represents a failed or denied event.
     */
    public boolean isUnsuccessful() {
        return switch (eventType.getOutcome()) {
            case FAILURE, DENIED -> true;
            case SUCCESS -> false;
        };
    }
}