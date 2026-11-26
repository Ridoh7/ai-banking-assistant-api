package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventCategory;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventOutcome;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

/**
 * Complete authorized representation of a single audit event.
 */
@Schema(description = "Detailed representation of an enterprise audit event")
public record AuditLogDetailResponse(

        @Schema(
                description = "Unique audit record identifier",
                example = "6d80c973-082b-4f0d-83e8-20e50a719930"
        )
        UUID id,

        AuditEventType eventType,

        AuditEventCategory eventCategory,

        AuditEventOutcome eventOutcome,

        @Schema(
                description = "Version of the audit event structure",
                example = "1"
        )
        Integer eventVersion,

        AuditActorResponse actor,

        AuditResourceResponse primaryResource,

        AuditResourceResponse secondaryResource,

        UUID correlationId,

        UUID requestId,

        UUID sessionId,

        String ipAddress,

        String userAgent,

        String deviceType,

        String deviceName,

        String browser,

        String operatingSystem,

        String httpMethod,

        String requestPath,

        String action,

        String description,

        String failureCode,

        String failureReason,

        @Schema(
                description = "Allowlisted and sanitized event-specific metadata"
        )
        JsonNode metadata,

        @Schema(
                description = "Selected resource state before the audited change"
        )
        JsonNode previousState,

        @Schema(
                description = "Selected resource state after the audited change"
        )
        JsonNode newState,

        @Schema(
                description = "SHA-256 integrity hash of the immutable audit record",
                example = "b93b6c5b9f8149091a3548f50a16eb07f27637cd5a47f706beaf66773ecb9df9"
        )
        String integrityHash,

        Instant createdAt
) {
}