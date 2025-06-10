package com.itcen.whiteboardserver;

import com.itcen.whiteboardserver.auth.service.TokenService;
import com.itcen.whiteboardserver.game.dto.request.RoomJoinRequest;
import com.itcen.whiteboardserver.game.dto.response.RoomInfoResponse;
import com.itcen.whiteboardserver.game.exception.ErrorResponse;
import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.enums.AuthProvider;
import com.itcen.whiteboardserver.member.enums.MemberRole;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseCookie;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional // 테스트 후 자동 롤백을 위한 애노테이션 추가
public class WebSocketRoomJoinTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    private WebSocketStompClient stompClient;
    private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
    private String wsUrl;

    // TokenService 주입
    @Autowired
    private TokenService tokenService;

    private Long testRoomCode;  // 테스트용 방 코드
    private final String testUserEmail = "test@example.com";  // 테스트용 사용자 이메일
    private Member testUser;

    @BeforeEach
    public void setup() {
        // STOMP 클라이언트 설정
        StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        WebSocketTransport transport = new WebSocketTransport(webSocketClient);
        SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(transport));

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        wsUrl = "ws://localhost:" + port + "/ws";

        // 테스트 사용자 준비 (필요한 경우)
        testUser = memberRepository.findByEmail(testUserEmail)
                .orElseGet(() -> {
                    Member newMember =
                            Member.builder()
                                    .email(testUserEmail)
                                    .password("<PASSWORD>")
                                    .nickname("Test User")
                                    .memberRole(Collections.singleton(MemberRole.MEMBER))
                                    .provider(AuthProvider.LOCAL)
                                    .build();
                    // 기타 필요한 설정
                    return memberRepository.save(newMember);
                });

        // 테스트 방을 준비하는 로직이 필요합니다 (API 호출 또는 직접 DB에 생성)
        // 여기서는 임의로 1번 방을 사용한다고 가정 (이 방은 존재하지 않음을 가정)
        testRoomCode = 1L;

        // TokenService를 사용하여 토큰 생성
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        List<String> roles = List.of(MemberRole.MEMBER.name());

        // TokenService의 issueTokens 메소드 호출
        ResponseCookie[] cookies = tokenService.issueTokens(testUserEmail, testUser.getNickname(), String.valueOf(testUser.getId()), roles, testUser.getProfileColor().name());

        // ResponseCookie에서 Set-Cookie 헤더 문자열로 변환
        String cookieHeader = cookies[0].toString(); // access_token 쿠키

        // 헤더에 access_token 쿠키 추가
        headers.add("Cookie", cookieHeader);
        log.info("테스트용 액세스 토큰 설정: {}", cookieHeader);

    }

    @Test
    @DisplayName("존재하지 않는 방에 참여 시 에러 응답 확인")
    public void testJoinNonExistingRoomShouldReturnError
            () throws InterruptedException, ExecutionException, TimeoutException {
        // 결과를 저장할 CompletableFuture 객체
        CompletableFuture<RoomInfoResponse> roomInfoResponseFuture = new CompletableFuture<>();
        CompletableFuture<ErrorResponse> errorResponseFuture = new CompletableFuture<>();

        // StompSession 핸들러 설정
        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                log.info("연결 성공: {}", session.getSessionId());

                // 방 정보 업데이트를 구독
                session.subscribe("/topic/room/" + testRoomCode, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return RoomInfoResponse.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        log.info("방 정보 업데이트 수신: {}", payload);
                        roomInfoResponseFuture.complete((RoomInfoResponse) payload);
                    }
                });

                // 에러 메시지 구독
                session.subscribe("/user/queue/errors", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ErrorResponse.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        log.error("에러 메시지 수신: {}", payload);
                        errorResponseFuture.complete((ErrorResponse) payload);
                    }
                });

                // 방 참여 메시지 전송
                RoomJoinRequest request = new RoomJoinRequest(testRoomCode);
                session.send("/app/room/join", request);
                log.info("방 참여 요청 전송: {}", request);
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.error("STOMP 세션 예외 발생", exception);
                errorResponseFuture.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                log.error("WebSocket 전송 오류 발생", exception);
                errorResponseFuture.completeExceptionally(exception);
            }
        };

        // WebSocket 연결 및 STOMP 세션 시작
        StompSession stompSession = stompClient.connectAsync(wsUrl, headers, sessionHandler).get(10, TimeUnit.SECONDS);
        assertNotNull(stompSession);
        log.info("STOMP 세션 연결됨: {}", stompSession.getSessionId());

        try {
            // 두 Future 중 하나가 완료될 때까지 대기
            Object result = CompletableFuture.anyOf(roomInfoResponseFuture, errorResponseFuture)
                    .get(15, TimeUnit.SECONDS);

            // 테스트 로직 변경: 에러 응답을 기대합니다 (방이 존재하지 않으므로)
            if (result instanceof ErrorResponse error) {
                log.info("방 참여 실패 (예상된 결과): {}", error);
                // 에러 메시지가 예상대로 왔는지 확인
                assertNotNull(error.getMessage());
                // 여기서 필요한 추가 검증 가능 (예: 에러 타입 확인 등)
                assertEquals("방을 찾을 수 없습니다.", error.getMessage(), "방이 존재하지 않는다는 에러 메시지가 와야 합니다");
            } else if (result instanceof RoomInfoResponse response) {
                // 방이 존재하지 않으므로 이 경우는 실패해야 함
                fail("방이 존재하지 않아야 하는데 방 정보가 반환되었습니다: " + response);
            }
        } finally {
            // 연결 종료
            if (stompSession.isConnected()) {
                stompSession.disconnect();
                log.info("STOMP 세션 연결 종료");
            }
        }
    }

    @AfterEach
    public void cleanup() {
        // 테스트에서 추가적으로 필요한 정리 작업이 있다면 여기서 수행
        // @Transactional이 DB 롤백을 처리하므로 추가 DB 정리는 필요 없음
        log.info("테스트 종료 및 정리 작업 완료");
    }
}