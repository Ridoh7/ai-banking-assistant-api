package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditEventCommand;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditResourceReference;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.request.AuditLogSearchRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogDetailResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.response.AuditLogSummaryResponse;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditResourceType;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.exception.AuditLogNotFoundException;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.mapper.AuditLogMapper;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.repository.AuditLogRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.specification.AuditLogSpecifications;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditIntegrityService;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditLogQueryService;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditLogService;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.BadRequestException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@RequiredArgsConstructor
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private static final int MAXIMUM_PAGE_SIZE = 100;

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id",
            "eventType",
            "eventCategory",
            "eventOutcome",
            "actorId",
            "actorType",
            "resourceType",
            "resourceId",
            "sessionId",
            "correlationId",
            "requestId",
            "ipAddress",
            "httpMethod",
            "requestPath",
            "createdAt"
    );

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;
    private final AuditIntegrityService integrityService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Page<AuditLogSummaryResponse> search(
            AuditLogSearchRequest searchRequest,
            Pageable pageable
    ) {
        validateSearch(searchRequest, pageable);

        Page<AuditLogSummaryResponse> result = auditLogRepository
                .findAll(
                        AuditLogSpecifications.from(searchRequest),
                        pageable
                )
                .map(auditLogMapper::toSummary);

        recordSearchEvent(searchRequest, pageable, result);
        return result;
    }

    @Override
    @Transactional
    public AuditLogDetailResponse findById(UUID auditId) {
        AuditLog auditLog = findAuditLog(auditId);
        AuditLogDetailResponse response = auditLogMapper.toDetail(auditLog);

        auditLogService.record(
                AuditEventCommand.builder()
                        .eventType(AuditEventType.AUDIT_LOG_VIEWED)
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.AUDIT_LOG,
                                auditId
                        ))
                        .description("Administrator viewed an audit log")
                        .build()
        );

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifyIntegrity(UUID auditId) {
        return integrityService.verify(findAuditLog(auditId));
    }

    private AuditLog findAuditLog(UUID auditId) {
        return auditLogRepository.findById(auditId)
                .orElseThrow(() -> new AuditLogNotFoundException(auditId));
    }

    private void validateSearch(
            AuditLogSearchRequest searchRequest,
            Pageable pageable
    ) {
        if (searchRequest.hasInvalidDateRange()) {
            throw new BadRequestException(
                    "Audit start date cannot be after end date"
            );
        }

        if (pageable.isUnpaged()) {
            throw new BadRequestException(
                    "Audit log searches must be paginated"
            );
        }

        if (pageable.getPageNumber() < 0) {
            throw new BadRequestException(
                    "Audit page number cannot be negative"
            );
        }

        if (pageable.getPageSize() < 1
                || pageable.getPageSize() > MAXIMUM_PAGE_SIZE) {
            throw new BadRequestException(
                    "Audit page size must be between 1 and "
                            + MAXIMUM_PAGE_SIZE
            );
        }

        for (Sort.Order order : pageable.getSort()) {
            if (!ALLOWED_SORT_PROPERTIES.contains(order.getProperty())) {
                throw new BadRequestException(
                        "Unsupported audit sort property: "
                                + order.getProperty()
                );
            }
        }
    }

    private void recordSearchEvent(
            AuditLogSearchRequest searchRequest,
            Pageable pageable,
            Page<AuditLogSummaryResponse> result
    ) {
        ObjectNode metadata = objectMapper.createObjectNode();
        metadata.put("page", pageable.getPageNumber());
        metadata.put("size", pageable.getPageSize());
        metadata.put("returnedElements", result.getNumberOfElements());
        metadata.put("totalElements", result.getTotalElements());
        metadata.put("filtered", hasAnyFilter(searchRequest));

        auditLogService.record(
                AuditEventCommand.builder()
                        .eventType(AuditEventType.AUDIT_LOG_SEARCHED)
                        .primaryResource(AuditResourceReference.of(
                                AuditResourceType.AUDIT_LOG,
                                "AUDIT_LOG_COLLECTION"
                        ))
                        .description("Administrator searched audit logs")
                        .metadata(metadata)
                        .build()
        );
    }

    private boolean hasAnyFilter(AuditLogSearchRequest request) {
        return request.getAuditId() != null
                || request.getEventType() != null
                || request.getEventCategory() != null
                || request.getEventOutcome() != null
                || request.getActorId() != null
                || request.getActorType() != null
                || hasText(request.getActorIdentifier())
                || hasText(request.getActorRole())
                || request.getResourceType() != null
                || hasText(request.getResourceId())
                || request.getSecondaryResourceType() != null
                || hasText(request.getSecondaryResourceId())
                || request.getSessionId() != null
                || request.getCorrelationId() != null
                || request.getRequestId() != null
                || hasText(request.getIpAddress())
                || hasText(request.getHttpMethod())
                || hasText(request.getRequestPath())
                || request.getStartDate() != null
                || request.getEndDate() != null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}