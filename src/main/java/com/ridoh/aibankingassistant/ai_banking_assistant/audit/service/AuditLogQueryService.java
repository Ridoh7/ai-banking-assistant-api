package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.request.AuditLogSearchRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogDetailResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogSummaryResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Administrative read contract for searching and retrieving audit records.
 *
 * Implementations must perform read-only operations and must never expose
 * entity mutation capabilities.
 */
public interface AuditLogQueryService {

    /**
     * Searches audit records using optional filters, pagination, and sorting.
     *
     * The implementation must:
     *
     * - Validate the supplied date range
     * - Enforce an allowlist of sortable properties
     * - Enforce the configured maximum page size
     * - Apply dynamic JPA Specifications
     * - Return lightweight summary responses
     * - Record an AUDIT_LOG_SEARCHED event without causing recursion
     *
     * @param searchRequest optional audit filters
     * @param pageable pagination and sorting instructions
     * @return page of matching audit summaries
     */
    Page<AuditLogSummaryResponse> search(
            @NotNull
            @Valid
            AuditLogSearchRequest searchRequest,
            @NotNull
            Pageable pageable
    );

    /**
     * Retrieves the full authorized view of one audit record.
     *
     * The implementation must record an AUDIT_LOG_VIEWED event while
     * preventing recursive audit generation.
     *
     * @param auditId audit record identifier
     * @return detailed audit response
     */
    AuditLogDetailResponse findById(
            @NotNull
            UUID auditId
    );

    /**
     * Verifies whether the persisted record's integrity hash matches
     * its immutable stored content.
     *
     * This is initially an internal service capability and will not
     * automatically be exposed as a public endpoint.
     *
     * @param auditId audit record identifier
     * @return true when the record passes integrity verification
     */
    boolean verifyIntegrity(
            @NotNull
            UUID auditId
    );
}