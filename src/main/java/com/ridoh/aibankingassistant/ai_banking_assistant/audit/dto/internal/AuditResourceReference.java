package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.UUID;

/**
 * Generic reference to a resource affected by an audited operation.
 *
 * Resource identifiers are represented as strings because different
 * resources may use UUIDs, transaction references, account identifiers,
 * session identifiers, or external references.
 */
@Builder
public record AuditResourceReference(

        @NotNull
        AuditResourceType resourceType,

        @NotNull
        @Size(max = 100)
        String resourceId,

        @Size(max = 255)
        String resourceName
) {

    /**
     * Convenience factory for resources identified by UUID.
     */
    public static AuditResourceReference of(
            AuditResourceType resourceType,
            UUID resourceId
    ) {
        return AuditResourceReference.builder()
                .resourceType(resourceType)
                .resourceId(resourceId.toString())
                .build();
    }

    /**
     * Convenience factory for resources identified by a string reference.
     */
    public static AuditResourceReference of(
            AuditResourceType resourceType,
            String resourceId
    ) {
        return AuditResourceReference.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .build();
    }

    /**
     * Convenience factory with a safe display name.
     */
    public static AuditResourceReference of(
            AuditResourceType resourceType,
            String resourceId,
            String resourceName
    ) {
        return AuditResourceReference.builder()
                .resourceType(resourceType)
                .resourceId(resourceId)
                .resourceName(resourceName)
                .build();
    }
}