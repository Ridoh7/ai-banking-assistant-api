package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditActorType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventCategory;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventOutcome;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/**
 * Complete immutable set of values used to calculate an audit record's
 * integrity hash.
 *
 * This record prevents long parameter lists and ensures the same values
 * used to build the entity are used during hash generation.
 */
@Builder
public record AuditIntegrityPayload(

        UUID id,

        AuditEventType eventType,

        AuditEventCategory eventCategory,

        AuditEventOutcome eventOutcome,

        Integer eventVersion,

        Long actorId,

        AuditActorType actorType,

        String actorIdentifier,

        String actorRole,

        AuditResourceType resourceType,

        String resourceId,

        String resourceName,

        AuditResourceType secondaryResourceType,

        String secondaryResourceId,

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

        JsonNode metadata,

        JsonNode previousState,

        JsonNode newState,

        Instant createdAt
) {
}