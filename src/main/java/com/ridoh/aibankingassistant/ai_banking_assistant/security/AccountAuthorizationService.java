package com.ridoh.aibankingassistant.ai_banking_assistant.security;

import com.ridoh.aibankingassistant.ai_banking_assistant.account.entity.Account;
import com.ridoh.aibankingassistant.ai_banking_assistant.common.exception.ForbiddenException;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.Role;
import com.ridoh.aibankingassistant.ai_banking_assistant.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountAuthorizationService {

    private static final String ACCOUNT_ACCESS_DENIED = "You do not have permission to access this account";

    private final CurrentUserService currentUserService;

    public void validateAccountAccess(Account account) {
        User currentUser = currentUserService.getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        validateOwner(account, currentUser);
    }

    public void validateAccountOwnership(Account account) {
        User currentUser = currentUserService.getCurrentUser();
        validateOwner(account, currentUser);
    }

    private void validateOwner(Account account, User currentUser) {
        if (!account.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(ACCOUNT_ACCESS_DENIED);
        }
    }
}
