package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal;

import lombok.Builder;

import java.util.UUID;

/**
 * Snapshot of the HTTP, session, device, and correlation context
 * associated with an audited operation.
 *
 * The context provider creates this object from trusted application
 * and Spring Security information. Business services must not populate
 * these fields directly from untrusted request parameters.
 *
 * All fields except correlationId and requestId may be absent when an
 * event occurs outside an HTTP request, such as a scheduled system task.
 */
@Builder
public record AuditRequestContext(

        /**
         * Identifier linking the audit event to related application logs
         * and operations.
         */
        UUID correlationId,

        /**
         * Identifier for the individual request that produced the event.
         */
        UUID requestId,

        /**
         * Authenticated session identifier extracted from the trusted
         * JWT or security context.
         */
        UUID sessionId,

        /**
         * Resolved client IP address.
         */
        String ipAddress,

        /**
         * Original sanitized User-Agent value.
         */
        String userAgent,

        /**
         * Broad device category, such as DESKTOP, MOBILE, TABLET,
         * BOT, or UNKNOWN.
         */
        String deviceType,

        /**
         * More specific detected device description.
         */
        String deviceName,

        /**
         * Detected browser name and optional major version.
         */
        String browser,

        /**
         * Detected operating-system name and optional version.
         */
        String operatingSystem,

        /**
         * HTTP method associated with the audited operation.
         */
        String httpMethod,

        /**
         * Request path without secret query-string values.
         */
        String requestPath
) {

    /**
     * Creates context for an operation that is not associated with
     * an HTTP request.
     */
    public static AuditRequestContext systemContext() {
        return AuditRequestContext.builder()
                .correlationId(UUID.randomUUID())
                .requestId(UUID.randomUUID())
                .build();
    }
}