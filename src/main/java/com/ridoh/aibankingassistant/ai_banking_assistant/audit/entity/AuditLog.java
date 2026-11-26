package com.ridoh.aibankingassistant.ai_banking_assistant.audit.entity;

import com.fasterxml.jackson.databind.JsonNode;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable persistent audit record.
 *
 * This entity represents an append-only record of an important banking,
 * authentication, authorization, session, administrative, security,
 * transaction, or system operation.
 *
 * Audit records must never be updated or deleted by application code.
 * Immutability is enforced through:
 *
 * 1. No public setters
 * 2. Hibernate {@link Immutable}
 * 3. Columns marked as non-updatable
 * 4. No audit update or deletion service methods
 * 5. PostgreSQL UPDATE and DELETE prevention trigger
 */
@Entity
@Table(name = "audit_logs")
@Immutable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(
        onlyExplicitlyIncluded = true
)
public class AuditLog {

    /**
     * Current audit-event schema version.
     */
    public static final int CURRENT_EVENT_VERSION = 1;

    @Id
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    @ToString.Include
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_type",
            nullable = false,
            length = 100,
            updatable = false
    )
    @ToString.Include
    private AuditEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_category",
            nullable = false,
            length = 50,
            updatable = false
    )
    private AuditEventCategory eventCategory;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "event_outcome",
            nullable = false,
            length = 30,
            updatable = false
    )
    @ToString.Include
    private AuditEventOutcome eventOutcome;

    @Column(
            name = "event_version",
            nullable = false,
            updatable = false
    )
    private Integer eventVersion;

    // ========================================================================
    // ACTOR SNAPSHOT
    // ========================================================================

    @Column(
            name = "actor_id",
            updatable = false
    )
    @ToString.Include
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "actor_type",
            nullable = false,
            length = 30,
            updatable = false
    )
    private AuditActorType actorType;

    /**
     * Sanitized actor identifier.
     *
     * Examples:
     * - Masked email address
     * - Stable service identifier
     * - BANKING_SYSTEM
     *
     * Passwords, access tokens, refresh tokens and credentials
     * must never be stored here.
     */
    @Column(
            name = "actor_identifier",
            length = 255,
            updatable = false
    )
    private String actorIdentifier;

    @Column(
            name = "actor_role",
            length = 50,
            updatable = false
    )
    private String actorRole;

    // ========================================================================
    // PRIMARY RESOURCE
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "resource_type",
            length = 50,
            updatable = false
    )
    private AuditResourceType resourceType;

    /**
     * Generic resource identifier.
     *
     * This value is stored as a String because resources may use UUIDs,
     * transaction references, account identifiers, or other identifier types.
     */
    @Column(
            name = "resource_id",
            length = 100,
            updatable = false
    )
    @ToString.Include
    private String resourceId;

    @Column(
            name = "resource_name",
            length = 255,
            updatable = false
    )
    private String resourceName;

    // ========================================================================
    // SECONDARY RESOURCE
    // ========================================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "secondary_resource_type",
            length = 50,
            updatable = false
    )
    private AuditResourceType secondaryResourceType;

    @Column(
            name = "secondary_resource_id",
            length = 100,
            updatable = false
    )
    private String secondaryResourceId;

    // ========================================================================
    // REQUEST AND SESSION CONTEXT
    // ========================================================================

    @Column(
            name = "correlation_id",
            nullable = false,
            updatable = false
    )
    @ToString.Include
    private UUID correlationId;

    @Column(
            name = "request_id",
            updatable = false
    )
    private UUID requestId;

    @Column(
            name = "session_id",
            updatable = false
    )
    private UUID sessionId;

    @Column(
            name = "ip_address",
            length = 45,
            updatable = false
    )
    private String ipAddress;

    /**
     * Excluded from toString because user-agent strings may be long
     * and may contain client-controlled content.
     */
    @Column(
            name = "user_agent",
            columnDefinition = "TEXT",
            updatable = false
    )
    private String userAgent;

    @Column(
            name = "device_type",
            length = 50,
            updatable = false
    )
    private String deviceType;

    @Column(
            name = "device_name",
            length = 150,
            updatable = false
    )
    private String deviceName;

    @Column(
            name = "browser",
            length = 100,
            updatable = false
    )
    private String browser;

    @Column(
            name = "operating_system",
            length = 100,
            updatable = false
    )
    private String operatingSystem;

    @Column(
            name = "http_method",
            length = 10,
            updatable = false
    )
    private String httpMethod;

    @Column(
            name = "request_path",
            length = 500,
            updatable = false
    )
    private String requestPath;

    // ========================================================================
    // EVENT DETAILS
    // ========================================================================

    @Column(
            name = "action",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String action;

    @Column(
            name = "description",
            length = 1000,
            updatable = false
    )
    private String description;

    @Column(
            name = "failure_code",
            length = 100,
            updatable = false
    )
    private String failureCode;

    /**
     * Sanitized business or security failure reason.
     *
     * This field must not contain stack traces, SQL statements,
     * credentials, tokens, or raw internal exception details.
     */
    @Column(
            name = "failure_reason",
            length = 500,
            updatable = false
    )
    private String failureReason;

    // ========================================================================
    // JSONB EVENT DATA
    // ========================================================================

    /**
     * Allowlisted and sanitized event-specific metadata.
     *
     * Complete request objects, entities, credentials, and arbitrary
     * exception objects must never be serialized into this field.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "metadata",
            columnDefinition = "jsonb",
            updatable = false
    )
    private JsonNode metadata;

    /**
     * Sanitized selected state before the audited change.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "previous_state",
            columnDefinition = "jsonb",
            updatable = false
    )
    private JsonNode previousState;

    /**
     * Sanitized selected state after the audited change.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(
            name = "new_state",
            columnDefinition = "jsonb",
            updatable = false
    )
    private JsonNode newState;

    // ========================================================================
    // INTEGRITY AND TIMING
    // ========================================================================

    /**
     * SHA-256 hexadecimal integrity hash.
     *
     * The hash is generated by the audit integrity service before
     * the entity is persisted.
     */
    @Column(
            name = "integrity_hash",
            nullable = false,
            length = 64,
            updatable = false
    )
    private String integrityHash;

    /**
     * Timezone-safe audit creation timestamp.
     *
     * This value is generated by the application before hashing and
     * persistence so that the exact same timestamp participates in
     * integrity-hash generation.
     */
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    @ToString.Include
    private Instant createdAt;
}