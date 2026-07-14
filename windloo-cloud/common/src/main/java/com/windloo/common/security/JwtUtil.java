package com.windloo.common.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

public class JwtUtil {

    public String sign(Long userId, List<String> roles, String secret, long expireSeconds) {
        SecretKey key = toKey(secret);
        Date now = new Date();
        return Jwts.builder()
                .claim(SecurityConstants.CLAIM_USER_ID, String.valueOf(userId))
                .claim(SecurityConstants.CLAIM_ROLES, roles.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token, String secret) {
        return Jwts.parser().verifyWith(toKey(secret)).build().parseSignedClaims(token).getPayload();
    }

    public boolean isValid(String token, String secret) {
        try { parse(token, secret); return true; }
        catch (Exception e) { return false; }
    }

    private SecretKey toKey(String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 256 bits (32 bytes)");
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}