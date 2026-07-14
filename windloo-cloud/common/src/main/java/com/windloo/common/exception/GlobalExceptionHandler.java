package com.windloo.common.exception;
import com.windloo.common.api.JsonResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BizException.class)
    public ResponseEntity<JsonResponse<Void>> handleBiz(BizException e) {
        return ResponseEntity.badRequest().body(JsonResponse.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<JsonResponse<Void>> handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe == null ? "参数校验失败" : fe.getDefaultMessage();
        return ResponseEntity.badRequest().body(JsonResponse.fail(400, msg));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonResponse<Void>> handleOther(Exception e) {
        log.error("unhandled exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(JsonResponse.fail(ErrorCode.SERVER_ERROR.getCode(), ErrorCode.SERVER_ERROR.getMsg()));
    }
}