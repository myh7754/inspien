package co.inspien.assignment.common.web;

import co.inspien.assignment.common.exception.ErrorCode;
import co.inspien.assignment.common.exception.InspienException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InspienException.class)
    public ResponseEntity<ErrorResponse> handleInspien(InspienException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.error("처리 실패 [{}]: {}", errorCode.name(), e.getMessage(), e.getCause());
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("예상치 못한 오류 발생", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                .body(ErrorResponse.from(errorCode, errorCode.getDefaultMessage()));
    }
}
