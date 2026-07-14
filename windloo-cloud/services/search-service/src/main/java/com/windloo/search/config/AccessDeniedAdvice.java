package com.windloo.search.config;
import com.windloo.common.api.JsonResponse;
import com.windloo.common.exception.ErrorCode;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AccessDeniedAdvice {
    @ExceptionHandler({AccessDeniedException.class, AuthorizationDeniedException.class})
    public ResponseEntity<JsonResponse<Void>> handle(Exception e) {
        return ResponseEntity.status(403).body(JsonResponse.fail(ErrorCode.FORBIDDEN.getCode(), ErrorCode.FORBIDDEN.getMsg()));
    }
}