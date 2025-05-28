package com.itcen.whiteboardserver.game.event;

import com.itcen.whiteboardserver.game.dto.request.RoomInfoRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomLeaveRequest;
import com.itcen.whiteboardserver.game.dto.response.RoomInfoResponse;
import com.itcen.whiteboardserver.game.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    /**
     * WebSocket 연결 해제 이벤트를 처리합니다. 이 메서드는 세션 연결 해제 이벤트를 수신하고,
     * 세션 속성에서 사용자 ID와 방 ID를 가져와서, 사용자가 방을 나가는 로직을 처리합니다.
     * 사용자 ID와 방 ID가 모두 사용 가능한 경우, 해당 서비스 로직을 호출하여 방에서 사용자를 제거합니다.
     *
     * @param event 연결 해제에 관한 정보(메시지 헤더 포함)를 담고 있는 SessionDisconnectEvent
     */

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());

        // 세션에서 사용자 ID와 방 ID를 가져옵니다
        Long memberId = (Long) headerAccessor.getSessionAttributes().get("memberId");
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");
        RoomLeaveRequest roomLeaveRequest = new RoomLeaveRequest(roomId);

        if (memberId != null && roomId != null) {
            // 사용자가 방에서 나가는 처리
            roomService.leaveRoom(roomLeaveRequest, memberId);
        }
    }

    /**
     * 방 참가자가 변경될 때 발생하는 이벤트를 처리합니다.
     * 이 메서드는 RoomParticipantChangedEvent를 수신하고 업데이트된 방 정보를 가져옵니다.
     * 그런 다음 업데이트된 방 정보를 지정된 방에 대한 WebSocket 토픽을 구독한 클라이언트들에게 전송합니다.
     *
     * @param event 방 ID를 포함하여 변경된 방 참가자에 대한 정보를 담고 있는 이벤트
     */

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomParticipantChangedEvent(RoomParticipantChangedEvent event) {
        Long roomId = event.getRoomCode();
        RoomInfoResponse roomInfoResponse = roomService.getRoomInfoByRoomCode(new RoomInfoRequest(roomId));
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomInfoResponse);
    }

    /**
     * 방 호스트 변경 이벤트를 처리합니다.
     * 이 메서드는 RoomHostChangedEvent를 수신하고 업데이트된 방 정보를 가져옵니다.
     * 그런 다음 업데이트된 방 정보를 지정된 방에 대한 WebSocket 토픽을 구독한 클라이언트들에게 전송합니다.
     *
     * @param event 방 ID를 포함하여 변경된 방 참가자에 대한 정보를 담고 있는 이벤트
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomHostChangedEvent(RoomHostChangedEvent event) {
        Long roomId = event.getRoomCode();
        RoomInfoResponse roomInfoResponse = roomService.getRoomInfoByRoomCode(new RoomInfoRequest(roomId));
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomInfoResponse);
    }


}