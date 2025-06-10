package com.itcen.whiteboardserver.security.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.Refill;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
public class RedisRateLimitingFilter extends OncePerRequestFilter {

    private final LettuceBasedProxyManager<String> bucketManager;
    // 익명용/IP, 인증용/유저+IP, 로그인용 세 가지 리미터
    private final Bandwidth anonSliding = Bandwidth.classic(10, Refill.greedy(1, Duration.ofSeconds(10)));
    private final Bandwidth userLoginSliding = Bandwidth.classic(10, Refill.greedy(1, Duration.ofSeconds(5)));
    private final Bandwidth userSliding = Bandwidth.classic(10, Refill.greedy(1, Duration.ofSeconds(5)));
    /* 설명. jwt authentication 전에 미리 체크 */
    private final JwtParser jwtParser;
    private final SecretKey key;

    public RedisRateLimitingFilter(LettuceBasedProxyManager<String> bucketManager, @Value("${jwt.secret}") String secret) {
        this.bucketManager = bucketManager;
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser  = Jwts.parser()
                .verifyWith(key)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {


        String ip = Optional.ofNullable(req.getHeader("X-Forwarded-For"))
                .filter(h -> !h.isBlank())
                .orElseGet(req::getRemoteAddr);

        String token = WebUtils.getCookie(req, "access_token") != null
                ? WebUtils.getCookie(req, "access_token").getValue()
                : null;

        String user = null;

        if(token != null) {
            try {
                Claims claims = jwtParser.parseSignedClaims(token)
                        .getPayload();
                user = claims.getSubject();
            } catch (Exception e) {
                log.error("Failed to parse JWT token: {}", e.getMessage());
            }
        }

        boolean isRefresh = req.getRequestURI().endsWith("/api/auth/oauth2/refresh");
        // 키 계산
        String bucketKey;
        Bandwidth limit;
        if (isRefresh && token != null && user != null) {
            // 리프레시 토큰은 인증된 유저만
            bucketKey = "rl:refresh:user:" + user + ":" + ip;
            limit = userSliding;
        } else if (isRefresh) {
            bucketKey = "rl:refresh:anon:" + ip;
            limit = anonSliding;
        } else {
            // 로그인 시도
            bucketKey = "rl:login:" + ip;
            limit = userLoginSliding;  // 로그인은 좀 더 느슨하게?
        }

        Bucket bucket = bucketManager.builder().build(
                bucketKey,
                () -> BucketConfiguration.builder().addLimit(limit).build()
        );
        if (!bucket.tryConsume(1)) {
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(Duration.ofNanos(limit.getRefillPeriodNanos()).getSeconds()));
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"error\":\"TOO_MANY_REQUESTS\"}");
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        // /api/auth/oauth2/login, /api/auth/refresh 두 경로만 필터링 대상
        return !(
                (req.getMethod().equals("POST") && req.getRequestURI().equals("/api/auth/oauth2/refresh")) ||
                (req.getMethod().equals("POST") && req.getRequestURI().equals("/api/auth/login"))
        );
    }

}