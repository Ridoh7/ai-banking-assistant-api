package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventCategory;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventOutcome;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight representation used in paginated audit-log searches.
 *
 * Large JSON metadata and state values are intentionally excluded.
 */
@Schema(description = "Summary of an enterprise audit event")
public record AuditLogSummaryResponse(

        @Schema(
                description = "Unique audit record identifier",
                example = "6d80c973-082b-4f0d-83e8-20e50a719930"
        )
        UUID id,

        @Schema(
                description = "Specific audit event type",
                example = "TRANSFER_SUCCESS"
        )
        AuditEventType eventType,

        @Schema(
                description = "High-level audit event category",
                example = "TRANSACTION"
        )
        AuditEventCategory eventCategory,

        @Schema(
                description = "Final outcome of the audited operation",
                example = "SUCCESS"
        )
        AuditEventOutcome eventOutcome,

        @Schema(
                description = "Stable action performed",
                example = "TRANSFER_FUNDS"
        )
        String action,

        AuditActorResponse actor,

        AuditResourceResponse primaryResource,

        AuditResourceResponse secondaryResource,

        @Schema(
                description = "Session that initiated the operation",
                example = "20e95445-19dd-44bf-9c61-c05d36dde185"
        )
        UUID sessionId,

        @Schema(
                description = "Correlation identifier used to trace the operation",
                example = "1cb536a4-e9c4-41cf-b86b-a79572579ab8"
        )
        UUID correlationId,

        @Schema(
                description = "Client IP address associated with the event",
                example = "192.168.1.20"
        )
        String ipAddress,

        @Schema(
                description = "Time the audit event was recorded",
                example = "2026-07-16T15:48:10Z"
        )
        Instant createdAt
) {
}