package com.itcen.whiteboardserver.global.exception;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalExceptionResponse {
    private final Integer code;
    private final String message;

    @JsonIgnore
    private final HttpStatus httpStatus;

    public GlobalExceptionResponse(GlobalErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
        this.httpStatus = errorCode.getHttpStatus();
    }

    public static GlobalExceptionResponse of(GlobalErrorCode errorCode) {
        return new GlobalExceptionResponse(errorCode);
    }

}
