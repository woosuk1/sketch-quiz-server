package com.itcen.whiteboardserver.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itcen.whiteboardserver.auth.service.TokenService;
import com.itcen.whiteboardserver.global.exception.GlobalCommonException;
import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import com.itcen.whiteboardserver.global.exception.GlobalExceptionResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

/**
 * JWT authentication filter: extracts access_token from HttpOnly cookie,
 * validates it, and populates SecurityContext.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(TokenService tokenService, ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Preflight(OPTIONS) 요청은 바로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("/api/auth/oauth2/refresh".equals(request.getRequestURI())) {
            // 그냥 filter chain pass (아무 인증도 안함)
            filterChain.doFilter(request, response);
            return;
        }

        // 1) HttpOnly 쿠키에서 액세스 토큰을 꺼냅니다.
//        String token = WebUtils.getCookie(request, "access_token") != null
//                ? Objects.requireNonNull(WebUtils.getCookie(request, "access_token")).getValue()
//                : null;

        // 1) frontend에서 보내주는 access token을 꺼냅니다.
        String token = Optional.ofNullable(request.getHeader("Authorization"))
                .filter(auth -> auth.startsWith("Bearer "))
                .map(auth -> auth.substring(7))
                .orElse(null);


        /* 설명.
         *  보호되는 api 접근 시, token == null -> filter chain을 타고
         *  40101 error를 보낼 시 front에서 refresh 경로로 요청을 보냄
         * */
        if (token != null) {
            try {
                // 2) 정상적인 경우: 토큰 검증 후 Authentication 반환
                Authentication auth = tokenService.authenticateAccess(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (GlobalCommonException invalid) {
                /* 설명. 이 때 catch 되는 예외는 서명 오류 등의 예외이다. */
                log.info("Invalid access token for {} {}: {}", request.getMethod(), request.getRequestURI(), invalid.getMessage());
                sendErrorResponse(response, GlobalErrorCode.INVALID_ACCESS_TOKEN);
                return;
            }
        }

        /* 설명. 필터 체인을 넘어가는 경우는
         *  1. 인증된 토큰
         *  2. token이 null일 시
         *  3. public api
         * */
        filterChain.doFilter(request, response);
    }


    private void sendErrorResponse(HttpServletResponse response, GlobalErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // JSON 직렬화: { "code": 40101, "message": "Access Token Expired" }
        GlobalExceptionResponse globalExceptionResponse = GlobalExceptionResponse.of(errorCode);
        String json = objectMapper.writeValueAsString(globalExceptionResponse);
        response.getWriter().write(json);
    }
}