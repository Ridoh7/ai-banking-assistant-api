package com.ridoh.aibankingassistant.ai_banking_assistant.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    private static Authentication getAuthentication() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user.");
        }

        return authentication;
    }

    public static String getCurrentUserEmail() {

        return getAuthentication().getName();
    }

    public static UUID getCurrentSessionId() {

        Object details = getAuthentication().getDetails();

        if (details instanceof UUID sessionId) {
            return sessionId;
        }

        throw new IllegalStateException("Current session ID not found.");
    }
}