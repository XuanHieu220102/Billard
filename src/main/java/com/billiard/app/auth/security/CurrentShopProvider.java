package com.billiard.app.auth.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves the currently authenticated user's shopId/userId from the
 * SecurityContext. This is the ONLY way business services should determine
 * shopId — never from request body/query params (see "Khong tin client",
 * technology rules section 37).
 */
@Component
public class CurrentShopProvider {

    public UUID getCurrentShopId() {
        return getCurrentUser().getShopId();
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    private AuthenticatedUser getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof AuthenticatedUser authenticatedUser)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return authenticatedUser;
    }
}
