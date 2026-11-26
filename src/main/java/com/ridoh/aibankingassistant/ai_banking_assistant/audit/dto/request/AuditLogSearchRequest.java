package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.request;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditActorType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventCategory;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventOutcome;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * Optional filters supported by the administrative audit-log search API.
 *
 * Pagination and sorting are handled separately through Spring Pageable.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuditLogSearchRequest {

    private UUID auditId;

    private AuditEventType eventType;

    private AuditEventCategory eventCategory;

    private AuditEventOutcome eventOutcome;

    private Long actorId;

    private AuditActorType actorType;

    @Size(max = 255)
    private String actorIdentifier;

    @Size(max = 50)
    private String actorRole;

    private AuditResourceType resourceType;

    @Size(max = 100)
    private String resourceId;

    private AuditResourceType secondaryResourceType;

    @Size(max = 100)
    private String secondaryResourceId;

    private UUID sessionId;

    private UUID correlationId;

    private UUID requestId;

    @Size(max = 45)
    private String ipAddress;

    @Size(max = 10)
    private String httpMethod;

    @Size(max = 500)
    private String requestPath;

    /**
     * Inclusive lower timestamp boundary.
     *
     * Example:
     * 2026-07-16T00:00:00Z
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant startDate;

    /**
     * Inclusive upper timestamp boundary.
     *
     * Example:
     * 2026-07-16T23:59:59Z
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Instant endDate;

    /**
     * Returns true when both date boundaries are present and invalid.
     */
    public boolean hasInvalidDateRange() {
        return startDate != null
                && endDate != null
                && startDate.isAfter(endDate);
    }
}