package com.itcen.whiteboardserver.security.filter;

import com.itcen.whiteboardserver.auth.service.TokenService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * JWT authentication filter: extracts access_token from HttpOnly cookie,
 * validates it, and populates SecurityContext.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1) HttpOnly 쿠키에서 액세스 토큰을 꺼냅니다.
        String token = WebUtils.getCookie(request, "access_token") != null
                ? Objects.requireNonNull(WebUtils.getCookie(request, "access_token")).getValue()
                : null;

        /* 설명.
         *  보호되는 api 접근 시, token == null -> filter chain을 타고
         *  40101 error를 보낼 시 front에서 refresh 경로로 요청을 보냄
        * */
        if (token != null) {
            try {
                // 2) 정상적인 경우: 토큰 검증 후 Authentication 반환
                Authentication auth = tokenService.authenticateAccess(token);

                if (auth != null) {
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (JwtException invalid) {
                /* 설명. 이 때 catch 되는 예외는 서명 오류 등의 예외이다. */
                log.info("Invalid access token for {} {}: {}", request.getMethod(), request.getRequestURI(), invalid.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token invalid");
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
}