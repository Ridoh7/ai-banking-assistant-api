package com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums;

/**
 * Types of resources that may be referenced by an audit record.
 *
 * The database stores these values as strings so new resource
 * types can be added without changing the table structure.
 */
public enum AuditResourceType {

    USER,

    ACCOUNT,

    TRANSACTION,

    SESSION,

    REFRESH_TOKEN,

    IDEMPOTENCY_RECORD,

    AUDIT_LOG,

    AUTHENTICATION,

    SYSTEM
}