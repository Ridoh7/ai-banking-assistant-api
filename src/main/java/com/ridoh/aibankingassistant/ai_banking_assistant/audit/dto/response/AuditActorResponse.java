package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditActorType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Actor information returned in an audit API response.
 */
@Schema(description = "Snapshot of the actor responsible for the audited operation")
public record AuditActorResponse(

        @Schema(
                description = "Internal identifier of the actor",
                example = "66e64647-ceb6-4b78-b3dc-80cf3a609c57"
        )
        Long actorId,

        @Schema(
                description = "Classification of the actor",
                example = "ADMIN"
        )
        AuditActorType actorType,

        @Schema(
                description = "Sanitized or masked actor identifier",
                example = "ri***@example.com"
        )
        String actorIdentifier,

        @Schema(
                description = "Role held by the actor when the event occurred",
                example = "ADMIN"
        )
        String actorRole
) {
}