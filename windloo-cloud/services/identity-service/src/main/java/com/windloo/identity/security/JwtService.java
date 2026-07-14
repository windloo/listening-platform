package com.windloo.identity.security;
import com.windloo.common.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class JwtService {
    private final JwtUtil jwt = new JwtUtil();
    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.expire-seconds}") private long expireSeconds;
    public String issue(Long userId, List<String> roles) {
        return jwt.sign(userId, roles, secret, expireSeconds);
    }
}