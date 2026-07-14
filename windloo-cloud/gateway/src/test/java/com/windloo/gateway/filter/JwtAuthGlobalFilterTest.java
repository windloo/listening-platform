package com.windloo.gateway.filter;
import com.windloo.common.security.JwtUtil;
import com.windloo.common.security.SecurityConstants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
})
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.import-check.enabled=false"
})
class JwtAuthGlobalFilterTest {
    @Autowired WebTestClient webClient;
    JwtUtil jwt = new JwtUtil();
    static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @TestConfiguration
    static class TestRoutes {
        @Bean
        RouteLocator testRoutes(RouteLocatorBuilder b) {
            return b.routes().route("identity", r -> r.path("/api/identity/**").uri("lb://identity-service")).build();
        }
    }

    @Test void protected_path_without_token_returns_401() {
        webClient.get().uri("/api/identity/me").exchange().expectStatus().isUnauthorized();
    }

    @Test void protected_path_with_valid_token_is_not_401() {
        String token = jwt.sign(7L, List.of("ADMIN"), SECRET, 60);
        webClient.get().uri("/api/identity/me")
                .header(SecurityConstants.AUTH_HEADER, SecurityConstants.BEARER + token)
                .exchange()
                .expectStatus().value(s -> assertNotEquals(401, s.intValue()));
    }

    @Test void whitelist_login_path_is_not_401() {
        webClient.post().uri("/api/identity/login/byUserName").exchange()
                .expectStatus().value(s -> assertNotEquals(401, s.intValue()));
    }
}