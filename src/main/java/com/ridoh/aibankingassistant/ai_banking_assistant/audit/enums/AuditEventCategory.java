package com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums;

/**
 * High-level classification of audit events.
 *
 * Categories provide broad filtering while AuditEventType identifies
 * the exact operation that occurred.
 */
public enum AuditEventCategory {

    AUTHENTICATION,

    AUTHORIZATION,

    SESSION,

    USER_MANAGEMENT,

    ACCOUNT_MANAGEMENT,

    TRANSACTION,

    ADMINISTRATION,

    SECURITY,

    SYSTEM
}