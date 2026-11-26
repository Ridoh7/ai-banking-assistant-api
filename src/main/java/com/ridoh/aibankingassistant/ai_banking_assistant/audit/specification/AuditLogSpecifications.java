package com.ridoh.aibankingassistant.ai_banking_assistant.audit.specification;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.request.AuditLogSearchRequest;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import java.time.Instant;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;

/**
 * Builds composable database predicates for administrative audit searches.
 */
public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> from(
            AuditLogSearchRequest request
    ) {
        Specification<AuditLog> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (request.getAuditId() != null) {
            specification = specification.and(
                    equal("id", request.getAuditId())
            );
        }
        if (request.getEventType() != null) {
            specification = specification.and(
                    equal("eventType", request.getEventType())
            );
        }
        if (request.getEventCategory() != null) {
            specification = specification.and(
                    equal("eventCategory", request.getEventCategory())
            );
        }
        if (request.getEventOutcome() != null) {
            specification = specification.and(
                    equal("eventOutcome", request.getEventOutcome())
            );
        }
        if (request.getActorId() != null) {
            specification = specification.and(
                    equal("actorId", request.getActorId())
            );
        }
        if (request.getActorType() != null) {
            specification = specification.and(
                    equal("actorType", request.getActorType())
            );
        }
        if (hasText(request.getActorIdentifier())) {
            specification = specification.and(
                    containsIgnoreCase(
                            "actorIdentifier",
                            request.getActorIdentifier()
                    )
            );
        }
        if (hasText(request.getActorRole())) {
            specification = specification.and(
                    equalIgnoreCase("actorRole", request.getActorRole())
            );
        }
        if (request.getResourceType() != null) {
            specification = specification.and(
                    equal("resourceType", request.getResourceType())
            );
        }
        if (hasText(request.getResourceId())) {
            specification = specification.and(
                    equalTrimmed("resourceId", request.getResourceId())
            );
        }
        if (request.getSecondaryResourceType() != null) {
            specification = specification.and(
                    equal(
                            "secondaryResourceType",
                            request.getSecondaryResourceType()
                    )
            );
        }
        if (hasText(request.getSecondaryResourceId())) {
            specification = specification.and(
                    equalTrimmed(
                            "secondaryResourceId",
                            request.getSecondaryResourceId()
                    )
            );
        }
        if (request.getSessionId() != null) {
            specification = specification.and(
                    equal("sessionId", request.getSessionId())
            );
        }
        if (request.getCorrelationId() != null) {
            specification = specification.and(
                    equal("correlationId", request.getCorrelationId())
            );
        }
        if (request.getRequestId() != null) {
            specification = specification.and(
                    equal("requestId", request.getRequestId())
            );
        }
        if (hasText(request.getIpAddress())) {
            specification = specification.and(
                    equalTrimmed("ipAddress", request.getIpAddress())
            );
        }
        if (hasText(request.getHttpMethod())) {
            specification = specification.and(
                    equalIgnoreCase("httpMethod", request.getHttpMethod())
            );
        }
        if (hasText(request.getRequestPath())) {
            specification = specification.and(
                    containsIgnoreCase(
                            "requestPath",
                            request.getRequestPath()
                    )
            );
        }
        if (request.getStartDate() != null) {
            specification = specification.and(
                    createdAtOnOrAfter(request.getStartDate())
            );
        }
        if (request.getEndDate() != null) {
            specification = specification.and(
                    createdAtOnOrBefore(request.getEndDate())
            );
        }

        return specification;
    }

    private static <T> Specification<AuditLog> equal(
            String field,
            T value
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(field), value);
    }

    private static Specification<AuditLog> equalTrimmed(
            String field,
            String value
    ) {
        String normalized = value.trim();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.<String>get(field), normalized);
    }

    private static Specification<AuditLog> equalIgnoreCase(
            String field,
            String value
    ) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(root.<String>get(field)),
                        normalized
                );
    }

    private static Specification<AuditLog> containsIgnoreCase(
            String field,
            String value
    ) {
        String normalized = "%"
                + value.trim().toLowerCase(Locale.ROOT)
                + "%";

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.<String>get(field)),
                        normalized
                );
    }

    private static Specification<AuditLog> createdAtOnOrAfter(
            Instant startDate
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.<Instant>get("createdAt"),
                        startDate
                );
    }

    private static Specification<AuditLog> createdAtOnOrBefore(
            Instant endDate
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.<Instant>get("createdAt"),
                        endDate
                );
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}