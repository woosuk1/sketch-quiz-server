// CanvasHandler.java
package itcen.whiteboardserver.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import itcen.whiteboardserver.entity.Stroke;
import itcen.whiteboardserver.repository.StrokeRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CanvasHandler extends TextWebSocketHandler {

    private final Map<String, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final StrokeRepository strokeRepo;

    public CanvasHandler(StrokeRepository strokeRepo) {
        this.strokeRepo = strokeRepo;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String roomId = getRoomId(session);
        roomSessions.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        List<Stroke> strokes = strokeRepo.findByRoomId(roomId);
        for (Stroke stroke : strokes) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "stroke");
            payload.put("userId", stroke.getUserId());
            payload.put("color", stroke.getColor());
            payload.put("width", stroke.getWidth());
            payload.put("points", stroke.getPoints());

            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.get("type").asText();
        String userId = json.get("userId").asText();
        String roomId = getRoomId(session);

        if ("stroke".equals(type)) {
            Stroke stroke = objectMapper.treeToValue(json, Stroke.class);
            stroke.setRoomId(roomId);
            strokeRepo.save(stroke);
        } else if ("undo".equals(type)) {
            List<Stroke> userStrokes = strokeRepo.findByRoomId(roomId).stream()
                    .filter(s -> s.getUserId().equals(userId))
                    .sorted(Comparator.comparing(Stroke::getCreatedAt).reversed())
                    .toList();
            if (!userStrokes.isEmpty()) {
                strokeRepo.deleteById(userStrokes.get(0).getId());
            }
        }

        // broadcast to room only
        for (WebSocketSession s : roomSessions.getOrDefault(roomId, Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(message);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String roomId = getRoomId(session);
        roomSessions.getOrDefault(roomId, Set.of()).remove(session);
    }

    private String getRoomId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return "default";
        String query = uri.getQuery();
        if (query != null && query.startsWith("roomId=")) {
            return query.split("=")[1];
        }
        return "default";
    }
}
