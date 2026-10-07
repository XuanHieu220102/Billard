package com.billiard.app.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;
import java.util.UUID;

/**
 * Spring Security principal carrying the identifiers extracted from the JWT.
 * {@code shopId} is the single source of truth for scoping every business
 * query — it is never read from client-supplied request data.
 */
@Getter
public class AuthenticatedUser extends User {

    private final UUID userId;
    private final UUID shopId;

    public AuthenticatedUser(UUID userId, UUID shopId, String phoneNumber) {
        super(phoneNumber, "", authorities());
        this.userId = userId;
        this.shopId = shopId;
    }

    private static List<GrantedAuthority> authorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }
}
