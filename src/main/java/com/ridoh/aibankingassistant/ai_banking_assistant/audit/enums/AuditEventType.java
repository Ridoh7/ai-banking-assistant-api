package com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Stable classification of all supported audit events.
 *
 * Each event type is permanently associated with:
 *
 * 1. A high-level audit category
 * 2. An expected event outcome
 * 3. A stable action name
 *
 * Keeping this information with the enum prevents event producers
 * from assigning inconsistent categories, outcomes, or action names.
 */
@Getter
@RequiredArgsConstructor
public enum AuditEventType {

    // ========================================================================
    // AUTHENTICATION EVENTS
    // ========================================================================

    USER_REGISTRATION_SUCCESS(
            AuditEventCategory.USER_MANAGEMENT,
            AuditEventOutcome.SUCCESS,
            "REGISTER_USER"
    ),

    USER_REGISTRATION_FAILURE(
            AuditEventCategory.USER_MANAGEMENT,
            AuditEventOutcome.FAILURE,
            "REGISTER_USER"
    ),

    ADMIN_REGISTRATION_SUCCESS(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.SUCCESS,
            "REGISTER_ADMIN"
    ),

    ADMIN_REGISTRATION_FAILURE(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.FAILURE,
            "REGISTER_ADMIN"
    ),

    LOGIN_SUCCESS(
            AuditEventCategory.AUTHENTICATION,
            AuditEventOutcome.SUCCESS,
            "AUTHENTICATE_USER"
    ),

    LOGIN_FAILURE(
            AuditEventCategory.AUTHENTICATION,
            AuditEventOutcome.FAILURE,
            "AUTHENTICATE_USER"
    ),

    TOKEN_REFRESH_SUCCESS(
            AuditEventCategory.AUTHENTICATION,
            AuditEventOutcome.SUCCESS,
            "REFRESH_ACCESS_TOKEN"
    ),

    TOKEN_REFRESH_FAILURE(
            AuditEventCategory.AUTHENTICATION,
            AuditEventOutcome.FAILURE,
            "REFRESH_ACCESS_TOKEN"
    ),

    REFRESH_TOKEN_REUSE_DETECTED(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "DETECT_REFRESH_TOKEN_REUSE"
    ),

    INVALID_ACCESS_TOKEN(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "VALIDATE_ACCESS_TOKEN"
    ),

    EXPIRED_ACCESS_TOKEN(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "VALIDATE_ACCESS_TOKEN"
    ),

    // ========================================================================
    // SESSION EVENTS
    // ========================================================================

    SESSION_CREATED(
            AuditEventCategory.SESSION,
            AuditEventOutcome.SUCCESS,
            "CREATE_SESSION"
    ),

    SESSION_REVOKED(
            AuditEventCategory.SESSION,
            AuditEventOutcome.SUCCESS,
            "REVOKE_SESSION"
    ),

    CURRENT_SESSION_LOGOUT(
            AuditEventCategory.SESSION,
            AuditEventOutcome.SUCCESS,
            "LOGOUT_CURRENT_SESSION"
    ),

    SPECIFIC_SESSION_LOGOUT(
            AuditEventCategory.SESSION,
            AuditEventOutcome.SUCCESS,
            "LOGOUT_SPECIFIC_SESSION"
    ),

    OTHER_SESSIONS_LOGOUT(
            AuditEventCategory.SESSION,
            AuditEventOutcome.SUCCESS,
            "LOGOUT_OTHER_SESSIONS"
    ),

    ALL_SESSIONS_LOGOUT(
            AuditEventCategory.SESSION,
            AuditEventOutcome.SUCCESS,
            "LOGOUT_ALL_SESSIONS"
    ),

    REVOKED_SESSION_ACCESS_ATTEMPT(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "VALIDATE_SESSION"
    ),

    SESSION_OWNERSHIP_VALIDATION_FAILURE(
            AuditEventCategory.AUTHORIZATION,
            AuditEventOutcome.DENIED,
            "VALIDATE_SESSION_OWNERSHIP"
    ),

    // ========================================================================
    // ACCOUNT EVENTS
    // ========================================================================

    ACCOUNT_CREATED(
            AuditEventCategory.ACCOUNT_MANAGEMENT,
            AuditEventOutcome.SUCCESS,
            "CREATE_ACCOUNT"
    ),

    ACCOUNT_CREATION_FAILURE(
            AuditEventCategory.ACCOUNT_MANAGEMENT,
            AuditEventOutcome.FAILURE,
            "CREATE_ACCOUNT"
    ),

    ACCOUNT_FREEZE_SUCCESS(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.SUCCESS,
            "FREEZE_ACCOUNT"
    ),

    ACCOUNT_FREEZE_FAILURE(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.FAILURE,
            "FREEZE_ACCOUNT"
    ),

    ACCOUNT_UNFREEZE_SUCCESS(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.SUCCESS,
            "UNFREEZE_ACCOUNT"
    ),

    ACCOUNT_UNFREEZE_FAILURE(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.FAILURE,
            "UNFREEZE_ACCOUNT"
    ),

    ACCOUNT_CLOSE_SUCCESS(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.SUCCESS,
            "CLOSE_ACCOUNT"
    ),

    ACCOUNT_CLOSE_FAILURE(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.FAILURE,
            "CLOSE_ACCOUNT"
    ),

    ACCOUNT_OWNERSHIP_VALIDATION_FAILURE(
            AuditEventCategory.AUTHORIZATION,
            AuditEventOutcome.DENIED,
            "VALIDATE_ACCOUNT_OWNERSHIP"
    ),

    FROZEN_ACCOUNT_OPERATION_ATTEMPT(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "VALIDATE_ACCOUNT_STATUS"
    ),

    CLOSED_ACCOUNT_OPERATION_ATTEMPT(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "VALIDATE_ACCOUNT_STATUS"
    ),

    // ========================================================================
    // DEPOSIT EVENTS
    // ========================================================================

    DEPOSIT_SUCCESS(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.SUCCESS,
            "DEPOSIT_FUNDS"
    ),

    DEPOSIT_FAILURE(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.FAILURE,
            "DEPOSIT_FUNDS"
    ),

    // ========================================================================
    // WITHDRAWAL EVENTS
    // ========================================================================

    WITHDRAWAL_SUCCESS(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.SUCCESS,
            "WITHDRAW_FUNDS"
    ),

    WITHDRAWAL_FAILURE(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.FAILURE,
            "WITHDRAW_FUNDS"
    ),

    // ========================================================================
    // TRANSFER EVENTS
    // ========================================================================

    TRANSFER_SUCCESS(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.SUCCESS,
            "TRANSFER_FUNDS"
    ),

    TRANSFER_FAILURE(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.FAILURE,
            "TRANSFER_FUNDS"
    ),

    TRANSFER_REJECTED(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.DENIED,
            "TRANSFER_FUNDS"
    ),

    INSUFFICIENT_BALANCE(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.FAILURE,
            "VALIDATE_AVAILABLE_BALANCE"
    ),

    // ========================================================================
    // IDEMPOTENCY EVENTS
    // ========================================================================

    TRANSACTION_IDEMPOTENCY_REPLAY(
            AuditEventCategory.TRANSACTION,
            AuditEventOutcome.SUCCESS,
            "REPLAY_IDEMPOTENT_RESPONSE"
    ),

    TRANSACTION_IDEMPOTENCY_CONFLICT(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "VALIDATE_IDEMPOTENCY_KEY"
    ),

    // ========================================================================
    // AUTHORIZATION AND SECURITY EVENTS
    // ========================================================================

    AUTHENTICATION_REQUIRED(
            AuditEventCategory.AUTHENTICATION,
            AuditEventOutcome.DENIED,
            "REQUIRE_AUTHENTICATION"
    ),

    AUTHORIZATION_DENIED(
            AuditEventCategory.AUTHORIZATION,
            AuditEventOutcome.DENIED,
            "AUTHORIZE_REQUEST"
    ),

    RESOURCE_ACCESS_DENIED(
            AuditEventCategory.AUTHORIZATION,
            AuditEventOutcome.DENIED,
            "AUTHORIZE_RESOURCE_ACCESS"
    ),

    SUSPICIOUS_ACTIVITY_DETECTED(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "DETECT_SUSPICIOUS_ACTIVITY"
    ),

    // ========================================================================
    // AUDIT ACCESS EVENTS
    // ========================================================================

    AUDIT_LOG_SEARCHED(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.SUCCESS,
            "SEARCH_AUDIT_LOGS"
    ),

    AUDIT_LOG_VIEWED(
            AuditEventCategory.ADMINISTRATION,
            AuditEventOutcome.SUCCESS,
            "VIEW_AUDIT_LOG"
    ),

    AUDIT_LOG_ACCESS_DENIED(
            AuditEventCategory.SECURITY,
            AuditEventOutcome.DENIED,
            "ACCESS_AUDIT_LOGS"
    ),

    // ========================================================================
    // SYSTEM EVENTS
    // ========================================================================

    SYSTEM_OPERATION_SUCCESS(
            AuditEventCategory.SYSTEM,
            AuditEventOutcome.SUCCESS,
            "EXECUTE_SYSTEM_OPERATION"
    ),

    SYSTEM_OPERATION_FAILURE(
            AuditEventCategory.SYSTEM,
            AuditEventOutcome.FAILURE,
            "EXECUTE_SYSTEM_OPERATION"
    );

    private final AuditEventCategory category;

    private final AuditEventOutcome outcome;

    private final String action;
}