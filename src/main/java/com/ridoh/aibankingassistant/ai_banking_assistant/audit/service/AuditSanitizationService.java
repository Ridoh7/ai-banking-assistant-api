package com.ridoh.aibankingassistant.ai_banking_assistant.audit.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Central contract for sanitizing, masking, and limiting information
 * before it is persisted in the audit table.
 *
 * Audit producers must not implement inconsistent masking logic
 * independently.
 */
public interface AuditSanitizationService {

    /**
     * Sanitizes a general text value and enforces the supplied maximum
     * persisted length.
     *
     * The implementation should remove or normalize unsafe control
     * characters while preserving useful investigation information.
     *
     * @param value original value
     * @param maximumLength maximum allowed persisted length
     * @return sanitized value or null
     */
    String sanitizeText(
            String value,
            int maximumLength
    );

    /**
     * Sanitizes an HTTP request path.
     *
     * Secret query-string values must not be persisted. The implementation
     * should normally retain only the path component.
     *
     * @param requestPath original request path or URI
     * @return sanitized path
     */
    String sanitizeRequestPath(String requestPath);

    /**
     * Sanitizes the client-controlled User-Agent header.
     *
     * @param userAgent original User-Agent
     * @return bounded and sanitized value
     */
    String sanitizeUserAgent(String userAgent);

    /**
     * Sanitizes a safe failure reason.
     *
     * Stack traces, SQL fragments, token values, credentials, filesystem
     * paths, and implementation-sensitive exception details must not be
     * persisted.
     *
     * @param failureReason candidate failure reason
     * @return sanitized business-safe failure reason
     */
    String sanitizeFailureReason(String failureReason);

    /**
     * Sanitizes allowlisted JSON metadata.
     *
     * The implementation must recursively reject or redact prohibited
     * property names such as:
     *
     * - password
     * - passwordHash
     * - token
     * - accessToken
     * - refreshToken
     * - authorization
     * - cookie
     * - pin
     * - cvv
     * - secret
     * - privateKey
     *
     * @param metadata candidate metadata
     * @return sanitized defensive copy or null
     */
    JsonNode sanitizeMetadata(JsonNode metadata);

    /**
     * Sanitizes selected previous or new state.
     *
     * @param state selected state fields
     * @return sanitized defensive copy or null
     */
    JsonNode sanitizeState(JsonNode state);

    /**
     * Masks an email address for audit display and persistence.
     *
     * Example:
     *
     * ridoh.lawal@example.com -> ri***@example.com
     *
     * @param email original email
     * @return masked email
     */
    String maskEmail(String email);

    /**
     * Masks an account number while retaining only the final four digits.
     *
     * Example:
     *
     * 0123456789 -> ******6789
     *
     * @param accountNumber original account number
     * @return masked account number
     */
    String maskAccountNumber(String accountNumber);

    /**
     * Produces a one-way SHA-256 representation of an idempotency key or
     * other value that should be correlated without storing plaintext.
     *
     * @param value sensitive correlatable value
     * @return lowercase SHA-256 hexadecimal hash or null
     */
    String hashSensitiveReference(String value);
}