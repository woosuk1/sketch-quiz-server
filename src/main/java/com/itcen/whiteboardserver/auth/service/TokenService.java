package com.itcen.whiteboardserver.auth.service;

import com.itcen.whiteboardserver.member.enums.MemberRole;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TokenService: Best-practice implementation for JWT-based auth
 * - Access & Refresh in HttpOnly SameSite=Lax cookies
 * - Refresh rotation with jti stored in Redis (TTL)
 * - Logout revokes all refresh tokens
 * - Clear separation: generate, validate, authenticate, rotate
 */
@Service
@Slf4j
public class TokenService {

    private final StringRedisTemplate redis;
    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;
    private final JwtParser jwtParser;

    public TokenService(StringRedisTemplate redis,
                        @Value("${jwt.secret}") String secret,
                        @Value("${jwt.access-token-validity-seconds}") long accessSeconds,
                        @Value("${jwt.refresh-token-validity-seconds}") long refreshSeconds) {
        this.redis = redis;
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser  = Jwts.parser()
                .verifyWith(key)
                .build();
        this.accessTtl = Duration.ofSeconds(accessSeconds);
        this.refreshTtl = Duration.ofSeconds(refreshSeconds);
    }

    /**
     * Issue new access + refresh tokens and set as HttpOnly cookies
     */
    public void issueTokens(String username, String nickname, String id, List<String> roles,
                            HttpServletResponse response) {
        // 1) 기존 키 하나만 삭제할 필요 없이, 덮어쓰기
        String setKey = "refresh:" + username;

        // 2) 새 토큰 생성
        String access  = createToken(username, nickname, id, roles, accessTtl);
        String refresh = createToken(username, nickname, id, roles, refreshTtl);

        // 3) 새 JTI만 저장 (overwrite)
        String jti = jwtParser.parseSignedClaims(refresh).getPayload().getId();
        redis.opsForValue()
                .set(setKey, jti, refreshTtl);

        // 4) 쿠키 세팅
        addCookie(response, "access_token",  access,  accessTtl,  "/");
        addCookie(response, "refresh_token", refresh, refreshTtl, "/api/auth/oauth2/refresh");
    }


    /**
     * Validate access token, return Authentication
     */
    public Authentication authenticateAccess(String token) {
        if (token == null) return null;
        try {
            Claims claims = jwtParser.parseSignedClaims(token)
                    .getPayload();
            String username = claims.getSubject();

            String id = (String) claims.get("id");
            String nickname = (String) claims.get("nickname");

            @SuppressWarnings("unchecked")
            List<String> rolesAsString = claims.get("roles", List.class);
            Set<MemberRole> rolesEnum = rolesAsString.stream()
                    .map(MemberRole::valueOf)     // ex: "ROLE_ADMIN" → MemberRole.ADMIN
                    .collect(Collectors.toSet());

            /* 설명. customPrincipal 생성 */
            CustomPrincipal principal = new CustomPrincipal(
                    Long.valueOf(id),
                    username,
                    nickname,
                    /* 빈 문자열이어도 무방(패스워드는 이미 검증된 상태) */ "",
                    rolesEnum
            );

            return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
        } catch (JwtException ex) {
            return null;
        }
    }

    /**
     * Rotate refresh token on expiry
     */
    public void rotateRefresh(HttpServletRequest request, HttpServletResponse response) {
        String old = resolveCookie(request);
        if (old == null) throw new JwtException("Missing refresh token");

        Jws<Claims> claims = jwtParser.parseSignedClaims(old);
        String username = claims.getPayload().getSubject();
        String id = claims.getPayload().get("id", String.class);
        String nickname = claims.getPayload().get("nickname", String.class);
        String oldJti = claims.getPayload().getId();

        // 1) Redis에 저장된 값과 비교
        String key      = "refresh:" + username;
        String storedJti = redis.opsForValue().get(key);
        if (!oldJti.equals(storedJti)) {
            throw new JwtException("Invalid refresh token");
        }
        // 2) 덮어쓰기 방식으로 신규 토큰 발급
        List<String> roles = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 재사용 issueTokens
        issueTokens(username, nickname, id, roles, response);
    }

    /**
     * Logout: revoke all refresh tokens for current user
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, CustomPrincipal principal) {
//        CustomPrincipal principal = (CustomPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null) {
            String user = principal.getEmail();
            log.debug("Logging out user: {}", principal.getEmail());

            redis.delete("refresh:" + user);
        }
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        SecurityContextHolder.clearContext();
    }

    // --- internal helpers ---


    private String createToken(String subject, String nickname, String id, List<String> roles, Duration ttl) {
        return Jwts.builder()
                .subject(subject)
                .id(UUID.randomUUID().toString())
//                .claim("roles", String.join(",", roles))
                .claim("roles", roles)
                .claim("nickname", nickname)
                .claim("id", id)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ttl.toMillis()))
                .signWith(key)
                .compact();
    }

    private String resolveCookie(HttpServletRequest req) {
        Cookie c = WebUtils.getCookie(req, "refresh_token");
        return c != null ? c.getValue() : null;
    }

    /** HttpOnly, Secure, SameSite=Lax 쿠키 추가 */
    private void addCookie(HttpServletResponse res,
                           String name, String value,
                           Duration ttl, String path) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
//                .secure(true)
                .sameSite("Lax")
                .path(path)
                .maxAge(ttl)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearCookie(HttpServletResponse res, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
//                .secure(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
