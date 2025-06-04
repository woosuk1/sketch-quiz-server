package com.itcen.whiteboardserver.security.handler;

import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final GlobalErrorCode errorCode;

    public CustomAuthenticationEntryPoint(GlobalErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(errorCode.getHttpStatus().value(), errorCode.getMsg());
    }
}