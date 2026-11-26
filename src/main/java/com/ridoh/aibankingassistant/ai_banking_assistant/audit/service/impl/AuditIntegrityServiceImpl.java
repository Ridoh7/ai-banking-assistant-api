package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditIntegrityPayload;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity.AuditLog;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.service.AuditIntegrityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Generates and verifies deterministic SHA-256 hashes for immutable
 * enterprise audit records.
 *
 * Hash generation is deterministic because:
 *
 * 1. Fields are appended in a fixed order.
 * 2. Every value is length-prefixed.
 * 3. Null values use one stable representation.
 * 4. JSON object properties are sorted alphabetically.
 * 5. JSON array order is preserved.
 * 6. UTF-8 encoding is always used.
 */
@Service
@RequiredArgsConstructor
public class AuditIntegrityServiceImpl
        implements AuditIntegrityService {

    private static final String HASH_ALGORITHM = "SHA-256";

    /**
     * Stable marker used for null values.
     *
     * This is different from an empty String.
     */
    private static final String NULL_VALUE = "<NULL>";

    /**
     * Separates individual canonical values.
     */
    private static final char FIELD_SEPARATOR = '|';

    private final ObjectMapper objectMapper;

    @Override
    public String generateHash(AuditIntegrityPayload payload) {
        Objects.requireNonNull(
                payload,
                "Audit integrity payload must not be null"
        );

        validateRequiredFields(payload);

        String canonicalRepresentation =
                buildCanonicalRepresentation(payload);

        byte[] calculatedHash = calculateSha256(
                canonicalRepresentation
        );

        return toLowercaseHex(calculatedHash);
    }

    @Override
    public boolean verify(AuditLog auditLog) {
        if (auditLog == null
                || auditLog.getIntegrityHash() == null
                || auditLog.getIntegrityHash().isBlank()) {
            return false;
        }

        AuditIntegrityPayload payload =
                mapToIntegrityPayload(auditLog);

        String calculatedHash = generateHash(payload);

        byte[] storedHashBytes = auditLog
                .getIntegrityHash()
                .toLowerCase()
                .getBytes(StandardCharsets.UTF_8);

        byte[] calculatedHashBytes = calculatedHash
                .getBytes(StandardCharsets.UTF_8);

        /*
         * MessageDigest.isEqual performs a timing-resistant comparison,
         * reducing information leakage compared with ordinary String equality.
         */
        return MessageDigest.isEqual(
                storedHashBytes,
                calculatedHashBytes
        );
    }

    /**
     * Creates one deterministic representation of all immutable fields.
     *
     * The order used here is part of the audit integrity contract.
     * Changing this ordering in a later release would require increasing
     * the event version and preserving support for the older hash format.
     */
    private String buildCanonicalRepresentation(
            AuditIntegrityPayload payload
    ) {
        StringBuilder canonical = new StringBuilder(2_048);

        appendField(canonical, payload.id());
        appendField(canonical, payload.eventType());
        appendField(canonical, payload.eventCategory());
        appendField(canonical, payload.eventOutcome());
        appendField(canonical, payload.eventVersion());

        appendField(canonical, payload.actorId());
        appendField(canonical, payload.actorType());
        appendField(canonical, payload.actorIdentifier());
        appendField(canonical, payload.actorRole());

        appendField(canonical, payload.resourceType());
        appendField(canonical, payload.resourceId());
        appendField(canonical, payload.resourceName());

        appendField(canonical, payload.secondaryResourceType());
        appendField(canonical, payload.secondaryResourceId());

        appendField(canonical, payload.correlationId());
        appendField(canonical, payload.requestId());
        appendField(canonical, payload.sessionId());

        appendField(canonical, payload.ipAddress());
        appendField(canonical, payload.userAgent());
        appendField(canonical, payload.deviceType());
        appendField(canonical, payload.deviceName());
        appendField(canonical, payload.browser());
        appendField(canonical, payload.operatingSystem());
        appendField(canonical, payload.httpMethod());
        appendField(canonical, payload.requestPath());

        appendField(canonical, payload.action());
        appendField(canonical, payload.description());
        appendField(canonical, payload.failureCode());
        appendField(canonical, payload.failureReason());

        appendJsonField(canonical, payload.metadata());
        appendJsonField(canonical, payload.previousState());
        appendJsonField(canonical, payload.newState());

        appendField(canonical, payload.createdAt());

        return canonical.toString();
    }

    private void appendField(
            StringBuilder canonical,
            Object value
    ) {
        String normalizedValue = normalizeValue(value);

        canonical
                .append(normalizedValue.length())
                .append(':')
                .append(normalizedValue)
                .append(FIELD_SEPARATOR);
    }

    private void appendJsonField(
            StringBuilder canonical,
            JsonNode value
    ) {
        String canonicalJson = canonicalizeJson(value);
        appendField(canonical, canonicalJson);
    }

    /**
     * Converts ordinary values into stable String representations.
     */
    private String normalizeValue(Object value) {
        if (value == null) {
            return NULL_VALUE;
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        if (value instanceof Instant instant) {
            return instant.toString();
        }

        return value.toString();
    }

    /**
     * Produces deterministic JSON.
     *
     * Object properties are recursively sorted alphabetically.
     * Array order is deliberately preserved because array ordering
     * may carry business meaning.
     */
    private String canonicalizeJson(JsonNode source) {
        if (source == null || source.isNull()) {
            return NULL_VALUE;
        }

        JsonNode canonicalNode = sortJsonNode(source);

        try {
            return objectMapper.writeValueAsString(canonicalNode);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to canonicalize audit JSON for integrity hashing",
                    exception
            );
        }
    }

    private JsonNode sortJsonNode(JsonNode source) {
        if (source == null || source.isNull()) {
            return objectMapper.getNodeFactory().nullNode();
        }

        if (source.isObject()) {
            return sortObjectNode((ObjectNode) source);
        }

        if (source.isArray()) {
            return sortArrayNode((ArrayNode) source);
        }

        /*
         * Scalar values are immutable Jackson nodes in normal usage.
         * deepCopy ensures the hash implementation never relies on or
         * modifies the caller's original node.
         */
        return source.deepCopy();
    }

    private ObjectNode sortObjectNode(ObjectNode source) {
        ObjectNode sortedObject =
                objectMapper.createObjectNode();

        Map<String, JsonNode> sortedFields =
                new TreeMap<>(Comparator.naturalOrder());

        Iterator<Map.Entry<String, JsonNode>> fields =
                source.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();

            sortedFields.put(
                    field.getKey(),
                    sortJsonNode(field.getValue())
            );
        }

        sortedFields.forEach(sortedObject::set);

        return sortedObject;
    }

    private ArrayNode sortArrayNode(ArrayNode source) {
        ArrayNode sortedArray =
                objectMapper.createArrayNode();

        for (JsonNode element : source) {
            sortedArray.add(sortJsonNode(element));
        }

        return sortedArray;
    }

    private byte[] calculateSha256(String canonicalRepresentation) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance(HASH_ALGORITHM);

            return digest.digest(
                    canonicalRepresentation.getBytes(
                            StandardCharsets.UTF_8
                    )
            );
        } catch (NoSuchAlgorithmException exception) {
            /*
             * SHA-256 is mandatory in Java implementations.
             * This represents an invalid runtime environment rather
             * than a recoverable business failure.
             */
            throw new IllegalStateException(
                    "SHA-256 hashing algorithm is unavailable",
                    exception
            );
        }
    }

    private String toLowercaseHex(byte[] bytes) {
        StringBuilder result =
                new StringBuilder(bytes.length * 2);

        for (byte currentByte : bytes) {
            result.append(
                    Character.forDigit(
                            (currentByte >>> 4) & 0x0F,
                            16
                    )
            );

            result.append(
                    Character.forDigit(
                            currentByte & 0x0F,
                            16
                    )
            );
        }

        return result.toString();
    }

    private AuditIntegrityPayload mapToIntegrityPayload(
            AuditLog auditLog
    ) {
        return AuditIntegrityPayload.builder()
                .id(auditLog.getId())
                .eventType(auditLog.getEventType())
                .eventCategory(auditLog.getEventCategory())
                .eventOutcome(auditLog.getEventOutcome())
                .eventVersion(auditLog.getEventVersion())

                .actorId(auditLog.getActorId())
                .actorType(auditLog.getActorType())
                .actorIdentifier(auditLog.getActorIdentifier())
                .actorRole(auditLog.getActorRole())

                .resourceType(auditLog.getResourceType())
                .resourceId(auditLog.getResourceId())
                .resourceName(auditLog.getResourceName())

                .secondaryResourceType(
                        auditLog.getSecondaryResourceType()
                )
                .secondaryResourceId(
                        auditLog.getSecondaryResourceId()
                )

                .correlationId(auditLog.getCorrelationId())
                .requestId(auditLog.getRequestId())
                .sessionId(auditLog.getSessionId())

                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .deviceType(auditLog.getDeviceType())
                .deviceName(auditLog.getDeviceName())
                .browser(auditLog.getBrowser())
                .operatingSystem(auditLog.getOperatingSystem())
                .httpMethod(auditLog.getHttpMethod())
                .requestPath(auditLog.getRequestPath())

                .action(auditLog.getAction())
                .description(auditLog.getDescription())
                .failureCode(auditLog.getFailureCode())
                .failureReason(auditLog.getFailureReason())

                .metadata(auditLog.getMetadata())
                .previousState(auditLog.getPreviousState())
                .newState(auditLog.getNewState())

                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    private void validateRequiredFields(
            AuditIntegrityPayload payload
    ) {
        requireField(payload.id(), "id");
        requireField(payload.eventType(), "eventType");
        requireField(payload.eventCategory(), "eventCategory");
        requireField(payload.eventOutcome(), "eventOutcome");
        requireField(payload.eventVersion(), "eventVersion");
        requireField(payload.actorType(), "actorType");
        requireField(payload.correlationId(), "correlationId");
        requireField(payload.action(), "action");
        requireField(payload.createdAt(), "createdAt");

        if (payload.eventVersion() < 1) {
            throw new IllegalArgumentException(
                    "Audit event version must be greater than zero"
            );
        }

        if (payload.action().isBlank()) {
            throw new IllegalArgumentException(
                    "Audit action must not be blank"
            );
        }
    }

    private void requireField(
            Object value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Required audit integrity field is missing: "
                            + fieldName
            );
        }
    }
}