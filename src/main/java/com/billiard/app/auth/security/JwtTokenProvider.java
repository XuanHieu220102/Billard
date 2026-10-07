package com.billiard.app.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWT access tokens. The token carries the user id
 * (subject) and shop id (custom claim) so every subsequent request can
 * resolve {@code shopId} without touching the database.
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_SHOP_ID = "shopId";
    private static final String CLAIM_PHONE = "phone";

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UUID userId, UUID shopId, String phoneNumber) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_SHOP_ID, shopId.toString())
                .claim(CLAIM_PHONE, phoneNumber)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public UUID extractShopId(Claims claims) {
        return UUID.fromString(claims.get(CLAIM_SHOP_ID, String.class));
    }

    public String extractPhoneNumber(Claims claims) {
        return claims.get(CLAIM_PHONE, String.class);
    }
}
