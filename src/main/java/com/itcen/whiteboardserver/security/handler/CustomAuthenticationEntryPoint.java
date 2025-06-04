package com.itcen.whiteboardserver.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import com.itcen.whiteboardserver.global.exception.GlobalExceptionResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final GlobalErrorCode errorCode;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationEntryPoint(GlobalErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // 1) HTTP 상태 코드 설정
        response.setStatus(errorCode.getHttpStatus().value());
        // 2) JSON 응답을 보내기 위한 Content-Type 설정
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 3) GlobalExceptionResponse 객체 생성
        GlobalExceptionResponse globalExceptionResponse = GlobalExceptionResponse.of(errorCode);

        // 4) ObjectMapper로 JSON으로 직렬화하여 바디에 쓰기
        String json = objectMapper.writeValueAsString(globalExceptionResponse);
        response.getWriter().write(json);
    }
}