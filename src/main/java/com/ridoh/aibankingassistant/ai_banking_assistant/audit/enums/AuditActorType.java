package com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums;

/**
 * Identifies the type of actor responsible for an audited operation.
 */
public enum AuditActorType {

    /**
     * A normal authenticated banking customer.
     */
    USER,

    /**
     * An authenticated administrator.
     */
    ADMIN,

    /**
     * An operation initiated internally by the application.
     */
    SYSTEM,

    /**
     * An unauthenticated client.
     *
     * Common examples include failed login attempts and requests
     * made without a valid access token.
     */
    ANONYMOUS,

    /**
     * A trusted internal or external service.
     *
     * This supports future integrations, scheduled workers,
     * event consumers, and inter-service communication.
     */
    SERVICE
}