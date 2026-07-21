package com.windloo.ai.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.windloo.common.api.JsonResponse;
import com.windloo.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.io.IOException;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final ObjectMapper mapper = new ObjectMapper();
    @Bean public HeaderAuthenticationFilter headerAuthenticationFilter() { return new HeaderAuthenticationFilter(); }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .accessDeniedHandler((req, resp, ex) -> writeJson(resp, 403, ErrorCode.FORBIDDEN))
                .authenticationEntryPoint((req, resp, ex) -> writeJson(resp, 401, ErrorCode.UNAUTHORIZED)))
            .addFilterBefore(headerAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    private void writeJson(HttpServletResponse resp, int status, ErrorCode ec) throws IOException {
        resp.setStatus(status);
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.getWriter().write(mapper.writeValueAsString(JsonResponse.fail(ec.getCode(), ec.getMsg())));
    }
}