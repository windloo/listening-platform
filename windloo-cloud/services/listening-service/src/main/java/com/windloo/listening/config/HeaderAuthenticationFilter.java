package com.windloo.listening.config;
import com.windloo.common.security.SecurityConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HeaderAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String userId = req.getHeader(SecurityConstants.HEADER_USER_ID);
        String roles = req.getHeader(SecurityConstants.HEADER_USER_ROLES);
        if (userId != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(userId, null, parseRoles(roles)));
        }
        chain.doFilter(req, resp);
    }
    private List<SimpleGrantedAuthority> parseRoles(String roles) {
        if (roles == null || roles.isBlank()) return List.of();
        String trimmed = roles.replaceAll("[\\[\\]\\s]", "");
        if (trimmed.isEmpty()) return List.of();
        return Arrays.stream(trimmed.split(","))
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }
}