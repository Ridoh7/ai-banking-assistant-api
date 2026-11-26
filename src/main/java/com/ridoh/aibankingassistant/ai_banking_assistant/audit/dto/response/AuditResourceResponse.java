package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Resource information returned in an audit API response.
 */
@Schema(description = "Resource affected by the audited operation")
public record AuditResourceResponse(

        @Schema(
                description = "Type of affected resource",
                example = "ACCOUNT"
        )
        AuditResourceType resourceType,

        @Schema(
                description = "Stable identifier of the affected resource",
                example = "5f767768-17cc-4c34-a922-944f20f3ce83"
        )
        String resourceId,

        @Schema(
                description = "Optional safe human-readable resource name",
                example = "Account ending in 4821"
        )
        String resourceName
) {
}