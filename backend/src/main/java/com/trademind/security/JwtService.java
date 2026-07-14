package com.trademind.security;

import com.trademind.auth.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles JWT creation, parsing and validation.
 * Access tokens carry roles as claims; refresh tokens are opaque and
 * persisted (hashed) server-side so they can be revoked (see RefreshToken entity).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        String roles = user.getRoles().stream()
                .map(r -> "ROLE_" + r.getName())
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public String generateOpaqueRefreshToken() {
        return UUID.randomUUID().toString() + "." + UUID.randomUUID();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public long getRefreshTokenExpirationMs() {
        return jwtProperties.getRefreshTokenExpirationMs();
    }
}
