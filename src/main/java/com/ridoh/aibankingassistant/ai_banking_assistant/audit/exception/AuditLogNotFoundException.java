package com.ridoh.aibankingassistant.ai_banking_assistant.audit.exception;

import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ResourceNotFoundException;
import java.util.UUID;

public class AuditLogNotFoundException extends ResourceNotFoundException {

    public AuditLogNotFoundException(UUID auditId) {
        super("Audit log not found with ID: " + auditId);
    }
}