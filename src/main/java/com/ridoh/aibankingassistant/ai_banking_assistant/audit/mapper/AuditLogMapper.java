package com.ridoh.aibankingassistant.ai_banking_assistant.audit.mapper;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditActorResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogDetailResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogSummaryResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditResourceResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import org.springframework.stereotype.Component;

/**
 * Maps immutable persistence entities to public, read-only audit responses.
 */
@Component
public class AuditLogMapper {

    public AuditLogSummaryResponse toSummary(AuditLog auditLog) {
        return new AuditLogSummaryResponse(
                auditLog.getId(),
                auditLog.getEventType(),
                auditLog.getEventCategory(),
                auditLog.getEventOutcome(),
                auditLog.getAction(),
                toActor(auditLog),
                toResource(
                        auditLog.getResourceType(),
                        auditLog.getResourceId(),
                        auditLog.getResourceName()
                ),
                toResource(
                        auditLog.getSecondaryResourceType(),
                        auditLog.getSecondaryResourceId(),
                        null
                ),
                auditLog.getSessionId(),
                auditLog.getCorrelationId(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }

    public AuditLogDetailResponse toDetail(AuditLog auditLog) {
        return new AuditLogDetailResponse(
                auditLog.getId(),
                auditLog.getEventType(),
                auditLog.getEventCategory(),
                auditLog.getEventOutcome(),
                auditLog.getEventVersion(),
                toActor(auditLog),
                toResource(
                        auditLog.getResourceType(),
                        auditLog.getResourceId(),
                        auditLog.getResourceName()
                ),
                toResource(
                        auditLog.getSecondaryResourceType(),
                        auditLog.getSecondaryResourceId(),
                        null
                ),
                auditLog.getCorrelationId(),
                auditLog.getRequestId(),
                auditLog.getSessionId(),
                auditLog.getIpAddress(),
                auditLog.getUserAgent(),
                auditLog.getDeviceType(),
                auditLog.getDeviceName(),
                auditLog.getBrowser(),
                auditLog.getOperatingSystem(),
                auditLog.getHttpMethod(),
                auditLog.getRequestPath(),
                auditLog.getAction(),
                auditLog.getDescription(),
                auditLog.getFailureCode(),
                auditLog.getFailureReason(),
                auditLog.getMetadata(),
                auditLog.getPreviousState(),
                auditLog.getNewState(),
                auditLog.getIntegrityHash(),
                auditLog.getCreatedAt()
        );
    }

    private AuditActorResponse toActor(AuditLog auditLog) {
        return new AuditActorResponse(
                auditLog.getActorId(),
                auditLog.getActorType(),
                auditLog.getActorIdentifier(),
                auditLog.getActorRole()
        );
    }

    private AuditResourceResponse toResource(
            AuditResourceType resourceType,
            String resourceId,
            String resourceName
    ) {
        if (resourceType == null && resourceId == null) {
            return null;
        }

        return new AuditResourceResponse(
                resourceType,
                resourceId,
                resourceName
        );
    }
}