package itcen.whiteboardserver.util;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CanvasHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("✅ WebSocket 연결됨: " + session.getId());
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("📩 메시지 수신 [" + session.getId() + "]: " + message.getPayload());

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(message);
                } catch (Exception e) {
                    System.err.println("❌ 메시지 전송 실패 to [" + s.getId() + "]: " + e.getMessage());
                    sessions.remove(s); // ❗ 죽은 세션 제거
                }
            } else {
                sessions.remove(s); // ❗ 열린 상태가 아니면 제거
            }
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        System.out.println("❎ WebSocket 연결 종료됨: " + session.getId() + ", 상태: " + status);
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        System.err.println("🚨 전송 중 오류 발생 [" + session.getId() + "]: " + exception.getMessage());
        exception.printStackTrace();
    }
}
