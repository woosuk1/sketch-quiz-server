package com.itcen.whiteboardserver.security.filter;

import com.itcen.whiteboardserver.auth.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
@Slf4j
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenService tokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // JWT 검증 및 사용자 인증 처리
                Authentication auth = tokenService.authenticateAccess(token);
                if (auth != null) {
                    accessor.setUser(auth);
                    log.info("WebSocket CONNECT 인증 성공: {}", auth.getName());
                } else {
                    log.info("WebSocket CONNECT 인증 실패: auth=null");
                }
            } else {
                throw new AccessDeniedException("Missing or invalid Authorization header");
            }
        }

        return message;
    }
}
