package com.windloo.common.security;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    private final JwtUtil jwt = new JwtUtil();

    @Test void sign_then_parse_roundtrip() {
        String token = jwt.sign(100L, List.of("ADMIN"), SECRET, 60);
        Claims c = jwt.parse(token, SECRET);
        assertEquals("100", c.get(SecurityConstants.CLAIM_USER_ID, String.class));
        assertEquals("[ADMIN]", c.get(SecurityConstants.CLAIM_ROLES, String.class));
    }

    @Test void isValid_true_for_good_token_false_for_tampered() {
        String token = jwt.sign(1L, List.of("USER"), SECRET, 60);
        assertTrue(jwt.isValid(token, SECRET));
        assertFalse(jwt.isValid(token + "x", SECRET));
    }

    @Test void sign_rejects_short_secret() {
        assertThrows(IllegalArgumentException.class, () -> jwt.sign(1L, List.of("USER"), "short", 60));
    }
}