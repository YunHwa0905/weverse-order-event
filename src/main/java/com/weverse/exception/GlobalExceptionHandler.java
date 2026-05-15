package com.weverse.exception;

import com.weverse.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            OrderNotFoundException.class,
            MemberNotFoundException.class,
            CouponNotFoundException.class,
            StockNotFoundException.class,
            ProductNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(BusinessException ex) {
        log.error("리소스를 찾을 수 없음: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler({
            InsufficientStockException.class,
            InvalidCouponException.class,
            InvalidOrderStatusException.class,
            InsufficientMembershipException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequestException(BusinessException ex) {
        log.error("잘못된 요청: {}", ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    // @Valid 검증 실패 시 필드별 오류 메시지를 첫 번째 것만 반환한다.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 유효하지 않습니다.");
        log.error("요청 값 검증 실패: {}", message);
        return new ErrorResponse(message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception ex) {
        log.error("서버 내부 오류: {}", ex.getMessage(), ex);
        return new ErrorResponse("서버 내부 오류가 발생했습니다.");
    }
}
