package com.windloo.common.security;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }
    @Test void currentUserId_parsesPrincipal() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("123", null, List.of()));
        assertEquals(123L, SecurityUtil.currentUserId());
    }
    @Test void isAdmin_trueForAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        assertTrue(SecurityUtil.isAdmin());
    }
    @Test void isAdmin_falseForUser() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("1", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        assertFalse(SecurityUtil.isAdmin());
    }
    @Test void currentUserId_nullWhenNoAuth() { assertNull(SecurityUtil.currentUserId()); }
}