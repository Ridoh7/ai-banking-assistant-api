package com.ridoh.aibankingassistant.ai_banking_assistant.audit.dto.internal;

import com.ridoh.aibankingassistant.ai_banking_assistant.audit.enums.AuditActorType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Immutable snapshot of the actor responsible for an audited operation.
 *
 * Actor information is captured at the time of the event so that the
 * audit trail remains meaningful even if the user's profile or role
 * later changes.
 *
 * Sensitive values such as passwords, access tokens, refresh tokens,
 * cookies, and authorization headers must never be placed here.
 */
@Builder
public record AuditActorSnapshot(

        Long actorId,

        @NotNull
        AuditActorType actorType,

        @Size(max = 255)
        String actorIdentifier,

        @Size(max = 50)
        String actorRole
) {

    /**
     * Creates a system actor for events initiated internally
     * by the banking application.
     */
    public static AuditActorSnapshot system() {
        return AuditActorSnapshot.builder()
                .actorType(AuditActorType.SYSTEM)
                .actorIdentifier("BANKING_SYSTEM")
                .actorRole("SYSTEM")
                .build();
    }

    /**
     * Creates an anonymous actor for unauthenticated requests.
     *
     * The supplied identifier must already be sanitized or masked.
     */
    public static AuditActorSnapshot anonymous(String sanitizedIdentifier) {
        return AuditActorSnapshot.builder()
                .actorType(AuditActorType.ANONYMOUS)
                .actorIdentifier(sanitizedIdentifier)
                .build();
    }
}