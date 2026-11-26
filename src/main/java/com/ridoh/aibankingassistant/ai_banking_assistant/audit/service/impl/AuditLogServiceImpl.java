package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.impl;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.context.AuditContextProvider;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditActorSnapshot;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditEventCommand;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditIntegrityPayload;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditRequestContext;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditResourceReference;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditEventOutcome;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.repository.AuditLogRepository;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditIntegrityService;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditLogService;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditSanitizationService;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Creates immutable and tamper-evident enterprise audit records.
 *
 * Successful critical events join the caller's transaction. Failed and
 * denied events use an independent transaction so their audit evidence
 * survives rollback of the rejected business operation.
 */
@Service
@Validated
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final int MAX_ACTOR_IDENTIFIER_LENGTH = 255;
    private static final int MAX_ACTOR_ROLE_LENGTH = 50;
    private static final int MAX_RESOURCE_ID_LENGTH = 100;
    private static final int MAX_RESOURCE_NAME_LENGTH = 255;
    private static final int MAX_IP_ADDRESS_LENGTH = 45;
    private static final int MAX_DEVICE_TYPE_LENGTH = 50;
    private static final int MAX_DEVICE_NAME_LENGTH = 150;
    private static final int MAX_BROWSER_LENGTH = 100;
    private static final int MAX_OPERATING_SYSTEM_LENGTH = 100;
    private static final int MAX_HTTP_METHOD_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 1_000;
    private static final int MAX_FAILURE_CODE_LENGTH = 100;

    private final AuditLogRepository auditLogRepository;
    private final AuditContextProvider auditContextProvider;
    private final AuditSanitizationService sanitizationService;
    private final AuditIntegrityService integrityService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UUID record(AuditEventCommand command) {
        requireCommand(command);

        if (command.isUnsuccessful()) {
            throw new IllegalArgumentException(
                    "Failure and denied audit events must use recordFailure"
            );
        }

        return persist(
                command,
                resolveActor(command),
                auditContextProvider.resolveRequestContext()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID recordFailure(AuditEventCommand command) {requireCommand(command);

        if (!command.isUnsuccessful()) {
            throw new IllegalArgumentException(
                    "Successful audit events must use record"
            );
        }

        return persist(
                command,
                resolveActor(command),
                auditContextProvider.resolveRequestContext()
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UUID recordSystemEvent(AuditEventCommand command) {
        requireCommand(command);

        return persist(
                command,
                AuditActorSnapshot.system(),
                AuditRequestContext.systemContext()
        );
    }

    private UUID persist(
            AuditEventCommand command,
            AuditActorSnapshot actor,
            AuditRequestContext requestContext
    ) {
        AuditActorSnapshot sanitizedActor = sanitizeActor(actor);
        AuditResourceReference primaryResource =
                sanitizeResource(command.primaryResource());
        AuditResourceReference secondaryResource =
                sanitizeResource(command.secondaryResource());

        UUID auditId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        AuditIntegrityPayload payload = AuditIntegrityPayload.builder()
                .id(auditId)
                .eventType(command.eventType())
                .eventCategory(command.eventType().getCategory())
                .eventOutcome(command.eventType().getOutcome())
                .eventVersion(AuditLog.CURRENT_EVENT_VERSION)

                .actorId(sanitizedActor.actorId())
                .actorType(sanitizedActor.actorType())
                .actorIdentifier(sanitizedActor.actorIdentifier())
                .actorRole(sanitizedActor.actorRole())

                .resourceType(primaryResource != null
                        ? primaryResource.resourceType()
                        : null)
                .resourceId(primaryResource != null
                        ? primaryResource.resourceId()
                        : null)
                .resourceName(primaryResource != null
                        ? primaryResource.resourceName()
                        : null)

                .secondaryResourceType(secondaryResource != null
                        ? secondaryResource.resourceType()
                        : null)
                .secondaryResourceId(secondaryResource != null
                        ? secondaryResource.resourceId()
                        : null)

                .correlationId(command.correlationId() != null
                        ? command.correlationId()
                        : requestContext.correlationId())
                .requestId(command.requestId() != null
                        ? command.requestId()
                        : requestContext.requestId())
                .sessionId(command.sessionId() != null
                        ? command.sessionId()
                        : requestContext.sessionId())

                .ipAddress(sanitizationService.sanitizeText(
                        requestContext.ipAddress(),
                        MAX_IP_ADDRESS_LENGTH
                ))
                .userAgent(sanitizationService.sanitizeUserAgent(
                        requestContext.userAgent()
                ))
                .deviceType(sanitizationService.sanitizeText(
                        requestContext.deviceType(),
                        MAX_DEVICE_TYPE_LENGTH
                ))
                .deviceName(sanitizationService.sanitizeText(
                        requestContext.deviceName(),
                        MAX_DEVICE_NAME_LENGTH
                ))
                .browser(sanitizationService.sanitizeText(
                        requestContext.browser(),
                        MAX_BROWSER_LENGTH
                ))
                .operatingSystem(sanitizationService.sanitizeText(
                        requestContext.operatingSystem(),
                        MAX_OPERATING_SYSTEM_LENGTH
                ))
                .httpMethod(sanitizeHttpMethod(requestContext.httpMethod()))
                .requestPath(sanitizationService.sanitizeRequestPath(
                        requestContext.requestPath()
                ))

                .action(command.eventType().getAction())
                .description(sanitizationService.sanitizeText(
                        command.description(),
                        MAX_DESCRIPTION_LENGTH
                ))
                .failureCode(sanitizationService.sanitizeText(
                        command.failureCode(),
                        MAX_FAILURE_CODE_LENGTH
                ))
                .failureReason(sanitizationService.sanitizeFailureReason(
                        command.failureReason()
                ))

                .metadata(sanitizationService.sanitizeMetadata(
                        command.metadata()
                ))
                .previousState(sanitizationService.sanitizeState(
                        command.previousState()
                ))
                .newState(sanitizationService.sanitizeState(
                        command.newState()
                ))
                .createdAt(createdAt)
                .build();

        String integrityHash = integrityService.generateHash(payload);
        AuditLog auditLog = toEntity(payload, integrityHash);

        auditLogRepository.save(auditLog);
        return auditId;
    }

    private AuditLog toEntity(
            AuditIntegrityPayload payload,
            String integrityHash
    ) {
        return AuditLog.builder()
                .id(payload.id())
                .eventType(payload.eventType())
                .eventCategory(payload.eventCategory())
                .eventOutcome(payload.eventOutcome())
                .eventVersion(payload.eventVersion())

                .actorId(payload.actorId())
                .actorType(payload.actorType())
                .actorIdentifier(payload.actorIdentifier())
                .actorRole(payload.actorRole())

                .resourceType(payload.resourceType())
                .resourceId(payload.resourceId())
                .resourceName(payload.resourceName())
                .secondaryResourceType(payload.secondaryResourceType())
                .secondaryResourceId(payload.secondaryResourceId())

                .correlationId(payload.correlationId())
                .requestId(payload.requestId())
                .sessionId(payload.sessionId())
                .ipAddress(payload.ipAddress())
                .userAgent(payload.userAgent())
                .deviceType(payload.deviceType())
                .deviceName(payload.deviceName())
                .browser(payload.browser())
                .operatingSystem(payload.operatingSystem())
                .httpMethod(payload.httpMethod())
                .requestPath(payload.requestPath())

                .action(payload.action())
                .description(payload.description())
                .failureCode(payload.failureCode())
                .failureReason(payload.failureReason())
                .metadata(payload.metadata())
                .previousState(payload.previousState())
                .newState(payload.newState())

                .integrityHash(integrityHash)
                .createdAt(payload.createdAt())
                .build();
    }

    private AuditActorSnapshot resolveActor(AuditEventCommand command) {
        return command.actor() != null
                ? command.actor()
                : auditContextProvider.resolveActor();
    }

    private AuditActorSnapshot sanitizeActor(AuditActorSnapshot actor) {
        Objects.requireNonNull(actor, "Audit actor must not be null");
        Objects.requireNonNull(
                actor.actorType(),
                "Audit actor type must not be null"
        );

        return AuditActorSnapshot.builder()
                .actorId(actor.actorId())
                .actorType(actor.actorType())
                .actorIdentifier(sanitizationService.sanitizeText(
                        actor.actorIdentifier(),
                        MAX_ACTOR_IDENTIFIER_LENGTH
                ))
                .actorRole(sanitizationService.sanitizeText(
                        actor.actorRole(),
                        MAX_ACTOR_ROLE_LENGTH
                ))
                .build();
    }

    private AuditResourceReference sanitizeResource(
            AuditResourceReference resource
    ) {
        if (resource == null) {
            return null;
        }

        Objects.requireNonNull(
                resource.resourceType(),
                "Audit resource type must not be null"
        );

        String resourceId = sanitizationService.sanitizeText(
                resource.resourceId(),
                MAX_RESOURCE_ID_LENGTH
        );

        if (resourceId == null || resourceId.isBlank()) {
            throw new IllegalArgumentException(
                    "Audit resource ID must not be blank"
            );
        }

        return AuditResourceReference.builder()
                .resourceType(resource.resourceType())
                .resourceId(resourceId)
                .resourceName(sanitizationService.sanitizeText(
                        resource.resourceName(),
                        MAX_RESOURCE_NAME_LENGTH
                ))
                .build();
    }

    private String sanitizeHttpMethod(String httpMethod) {
        String sanitized = sanitizationService.sanitizeText(
                httpMethod,
                MAX_HTTP_METHOD_LENGTH
        );

        return sanitized != null
                ? sanitized.toUpperCase(Locale.ROOT)
                : null;
    }

    private void requireCommand(AuditEventCommand command) {
        Objects.requireNonNull(command, "Audit event command must not be null");
        Objects.requireNonNull(
                command.eventType(),
                "Audit event type must not be null"
        );

        if (command.eventType().getOutcome() == AuditEventOutcome.SUCCESS
                && (command.failureCode() != null
                || command.failureReason() != null)) {
            throw new IllegalArgumentException(
                    "Successful audit events must not contain failure details"
            );
        }
    }
}