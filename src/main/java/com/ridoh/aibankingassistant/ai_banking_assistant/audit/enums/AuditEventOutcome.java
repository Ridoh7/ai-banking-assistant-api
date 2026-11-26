package com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums;

/**
 * Final result of an audited operation.
 */
public enum AuditEventOutcome {

    /**
     * The operation completed successfully.
     */
    SUCCESS,

    /**
     * The operation was permitted to execute but failed.
     */
    FAILURE,

    /**
     * The operation was rejected by authentication,
     * authorization, ownership, session, or security controls.
     */
    DENIED
}