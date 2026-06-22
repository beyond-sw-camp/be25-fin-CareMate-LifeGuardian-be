package com.caremate.lifeguardian.common;

import com.caremate.lifeguardian.common.exception.AuthException;
import com.caremate.lifeguardian.common.exception.BaseException;
import com.caremate.lifeguardian.common.exception.ErrorResponse;
import com.caremate.lifeguardian.common.exception.RemainingCustomerConflictException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(RemainingCustomerConflictException.class)
    public ResponseEntity<ApiResponse<java.util.Map<String, Long>>> handleRemainingCustomerConflictException(RemainingCustomerConflictException e) {
        log.warn("잔여 고객 존재로 퇴사 차단 예외 발생: {}", e.getMessage());
        java.util.Map<String, Long> data = java.util.Map.of("remainingCustomerCount", e.getRemainingCustomerCount());
        return ResponseEntity
                .status(409)
                .body(ApiResponse.fail(409, e.getMessage(), data));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(BaseException e) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getCode())
                .body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorResponse>>> handleValidationException(MethodArgumentNotValidException e) {
        List<ErrorResponse> errors = e.getBindingResult().getFieldErrors().stream()
                .map(ErrorResponse::of)
                .toList();

        log.warn("검증 실패 에러 발생: {}건", errors.size());

        return ResponseEntity
                .status(400)
                .body(ApiResponse.fail(400, "입력 데이터가 유효하지 않습니다.", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        log.warn("잘못된 요청 예외 발생: {}", e.getMessage());

        return ResponseEntity
                .status(400)
                .body(ApiResponse.error(400, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(
            IllegalStateException e
    ) {
        log.warn("처리 불가능한 상태 예외 발생: {}", e.getMessage());

        return ResponseEntity
                .status(409)
                .body(ApiResponse.error(409, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("예상치 못한 서버 오류 발생: ", e);
        return ResponseEntity
                .status(500)
                .body(ApiResponse.error(500, "서버 내부 오류가 발생했습니다."));
    }

    // 필터 전용 예외 처리
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthException(AuthException e) {
        return ResponseEntity.status(401).body(ApiResponse.fail(401, e.getMessage(), null));
    }

}