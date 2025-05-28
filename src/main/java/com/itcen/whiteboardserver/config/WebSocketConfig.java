package com.itcen.whiteboardserver.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:*")
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        if (request instanceof ServletServerHttpRequest) {
                            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();

                            // HTTP 요청 헤더에서 JWT 토큰 추출
                            String token = extractToken(httpServletRequest);

                            if (token != null) {
                                try {
                                    // JWT 토큰 검증 및 payload에서 subject(sub) 추출
                                    DecodedJWT decodedJWT = verifyToken(token);
                                    String subject = decodedJWT.getSubject();

                                    if (subject != null) {
                                        // 사용자 ID를 세션 속성에 저장
                                        Long memberId = Long.valueOf(subject);
                                        attributes.put("memberId", memberId);

                                        // 사용자별 메시지를 위한 Principal 설정
                                        attributes.put("username", subject);

                                        log.info("WebSocket 연결 인증 성공: userId={}", subject);
                                        return true;
                                    }
                                } catch (JWTVerificationException e) {
                                    log.error("JWT 토큰 검증 실패", e);
                                    return false;
                                } catch (NumberFormatException e) {
                                    log.error("유효하지 않은 사용자 ID 형식", e);
                                    return false;
                                }
                            }

                            log.warn("유효한 JWT 토큰이 없음");
                            return false;
                        }
                        return false;
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                        // 핸드셰이크 후 처리 (필요한 경우)
                    }

                    private String extractToken(HttpServletRequest request) {
                        // 헤더에서 Authorization 값 추출
                        String bearerToken = request.getHeader("Authorization");

                        // Authorization 헤더가 없는 경우 URL 파라미터에서 토큰 추출 시도
                        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
                            return request.getParameter("token");
                        }

                        // "Bearer " 접두사 제거
                        return bearerToken.substring(7);
                    }

                    private DecodedJWT verifyToken(String token) throws JWTVerificationException {
                        // JWT 검증기 생성
                        Algorithm algorithm = Algorithm.HMAC256(jwtSecret);
                        JWTVerifier verifier = JWT.require(algorithm).build();

                        // 토큰 검증 및 디코딩
                        return verifier.verify(token);
                    }
                })
                // WebSocket 연결을 위한 Principal(사용자 인증 정보)을 설정
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        final String username = (String) attributes.get("username");
                        return username != null ? () -> username : super.determineUser(request, wsHandler, attributes);
                    }
                })
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트에서 구독할 주제 접두사
        registry.enableSimpleBroker("/topic", "/user");

        // 서버로 메시지를 보낼 때 사용할 접두사
        registry.setApplicationDestinationPrefixes("/app");

        // 사용자별 목적지 접두사
        registry.setUserDestinationPrefix("/user");
    }
}