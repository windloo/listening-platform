package com.windloo.gateway.filter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windloo.common.api.JsonResponse;
import com.windloo.common.security.JwtUtil;
import com.windloo.common.security.SecurityConstants;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {
    private static final List<String> WHITELIST = List.of(
            "/api/identity/login", "/api/identity/forgot", "/api/identity/init", "/actuator", "/api/listening/categories", "/api/listening/albums", "/api/listening/episodes", "/api/file/files", "/api/search");
    private final JwtUtil jwt = new JwtUtil();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (WHITELIST.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }
        String auth = exchange.getRequest().getHeaders().getFirst(SecurityConstants.AUTH_HEADER);
        if (auth == null || !auth.startsWith(SecurityConstants.BEARER)
                || !jwt.isValid(auth.substring(SecurityConstants.BEARER.length()), secret)) {
            return unauthorized(exchange);
        }
        Claims c = jwt.parse(auth.substring(SecurityConstants.BEARER.length()), secret);
        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(SecurityConstants.HEADER_USER_ID, c.get(SecurityConstants.CLAIM_USER_ID, String.class))
                .header(SecurityConstants.HEADER_USER_ROLES, c.get(SecurityConstants.CLAIM_ROLES, String.class))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse resp = exchange.getResponse();
        resp.setStatusCode(HttpStatus.UNAUTHORIZED);
        resp.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] body = mapper.writeValueAsBytes(JsonResponse.fail(40100, "未认证"));
            return resp.writeWith(Mono.just(resp.bufferFactory().wrap(body)));
        } catch (Exception e) { return Mono.error(e); }
    }

    @Override public int getOrder() { return -100; }
}