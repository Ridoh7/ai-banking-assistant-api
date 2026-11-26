package com.ridoh.aibankingassistant.ai_banking_assistant.audit.context;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditActorSnapshot;
import com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal.AuditRequestContext;

import java.util.Optional;
import java.util.UUID;

/**
 * Provides trusted actor, request, session, network, and device context
 * for audit-event creation.
 *
 * Implementations should resolve information from:
 *
 * - Spring SecurityContextHolder
 * - Authenticated principal
 * - Trusted JWT claims
 * - Current HTTP request
 * - Existing device-detection infrastructure
 * - Correlation-ID infrastructure
 *
 * Business services must not trust client-supplied actor or session data.
 */
public interface AuditContextProvider {

    /**
     * Resolves the current authenticated actor.
     *
     * Returns an anonymous actor when no authenticated principal exists.
     *
     * @return trusted actor snapshot
     */
    AuditActorSnapshot resolveActor();

    /**
     * Resolves current request and device context.
     *
     * When no HTTP request is active, the implementation must return a
     * system-compatible context with generated correlation and request IDs.
     *
     * @return request-context snapshot
     */
    AuditRequestContext resolveRequestContext();

    /**
     * Resolves the trusted current session identifier.
     *
     * The session ID must come from the authenticated JWT or established
     * security context, never from a request parameter.
     *
     * @return current session ID when available
     */
    Optional<UUID> resolveSessionId();

    /**
     * Resolves the active request correlation identifier or generates one.
     *
     * @return correlation UUID
     */
    UUID resolveCorrelationId();

    /**
     * Resolves the active request identifier or generates one.
     *
     * @return request UUID
     */
    UUID resolveRequestId();

    /**
     * Indicates whether the current request has an authenticated actor.
     *
     * @return true when the security context contains an authenticated,
     * non-anonymous principal
     */
    boolean isAuthenticated();
}