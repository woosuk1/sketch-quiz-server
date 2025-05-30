package com.itcen.whiteboardserver.security.filter;

import com.itcen.whiteboardserver.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

        if (token != null) {
            try {
                Authentication auth = tokenService.authenticateAccess(token);
                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException ex) {
                // 만료된 토큰도 refresh 흐름으로 넘겨 주고 싶다면
                if ("/api/auth/oauth2/refresh".equals(request.getRequestURI())
                        && "POST".equals(request.getMethod())) {
                    request.setAttribute("expiredAccessToken", token);
                } else {
                    /* 설명. 민료된 토큰은 401 응답 보내기 */
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                            ex instanceof JwtValidationException
                                    ? "Access token invalid"
                                    : "Access token expired");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}