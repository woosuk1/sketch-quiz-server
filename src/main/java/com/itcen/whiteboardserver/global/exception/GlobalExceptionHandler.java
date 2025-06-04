package com.itcen.whiteboardserver.global.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.itcen.whiteboardserver")
public class GlobalExceptionHandler {
    @ExceptionHandler({NoHandlerFoundException.class, HttpRequestMethodNotSupportedException.class})
    public ResponseEntity<GlobalExceptionResponse> handleNoPageFoundException(Exception e, HttpServletRequest request) {
        log.error("지원되지 않는 요청: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.WRONG_ENTRY_POINT, e, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GlobalExceptionResponse> handleArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.error("인자 타입 불일치: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.INVALID_PARAMETER_FORMAT, e, request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GlobalExceptionResponse> handleMissingRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.error("필수 파라미터 누락: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.MISSING_REQUEST_PARAMETER, e, request);
    }

    @ExceptionHandler(GlobalCommonException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(GlobalCommonException e, HttpServletRequest request) {
        log.error("사용자 예외 처리: {}", e.getMessage(), e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", e.getErrorCode().getMsg());
        response.put("code", e.getErrorCode().getCode());
//        return new ResponseEntity<>(response, e.getErrorCode().getHttpStatus());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalExceptionResponse> handleServerException(Exception e, HttpServletRequest request) {
        log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.INTERNAL_SERVER_ERROR, e, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<GlobalExceptionResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e, HttpServletRequest request) {
        log.error("데이터 무결성 위반: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.DATA_INTEGRITY_VIOLATION, e, request);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<GlobalExceptionResponse> handleDataAccessResourceFailureException(DataAccessResourceFailureException e, HttpServletRequest request) {
        log.error("DB 연결 오류: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.DATABASE_CONNECTION_FAILURE, e, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("유효성 검사 실패: {}", e.getMessage(), e);
        return createErrorResponse(GlobalErrorCode.VALIDATION_FAIL, e, request);
    }

    private ResponseEntity<GlobalExceptionResponse> createErrorResponse(GlobalErrorCode errorCode, Exception e, HttpServletRequest request) {
        GlobalExceptionResponse response = GlobalExceptionResponse.of(errorCode);
        return ResponseEntity.status(response.getHttpStatus()).body(response);
    }

}
