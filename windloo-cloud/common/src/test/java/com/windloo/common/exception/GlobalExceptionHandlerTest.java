package com.windloo.common.exception;
import com.windloo.common.api.JsonResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test void bizException_returns_its_code() {
        ResponseEntity<JsonResponse<Void>> r = handler.handleBiz(new BizException(ErrorCode.USER_EXISTS));
        assertEquals(400, r.getStatusCode().value());
        assertEquals(40002, r.getBody().getCode());
    }

    @Test void unhandledException_returns_500() {
        ResponseEntity<JsonResponse<Void>> r = handler.handleOther(new RuntimeException("boom"));
        assertEquals(500, r.getStatusCode().value());
        assertEquals(50000, r.getBody().getCode());
        assertNull(r.getBody().getData());
    }
}