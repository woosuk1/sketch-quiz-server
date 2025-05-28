package com.itcen.whiteboardserver.game.controller;

import com.itcen.whiteboardserver.game.dto.request.RoomInfoRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomJoinRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomLeaveRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomRequest;
import com.itcen.whiteboardserver.game.dto.response.RoomInfoResponse;
import com.itcen.whiteboardserver.game.dto.response.RoomResponse;
import com.itcen.whiteboardserver.game.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

// WebSocket 엔드포인트 - 방 퇴장 요청 처리
@Controller
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final SimpMessagingTemplate messagingTemplate;

    // REST API 엔드포인트 - 방 생성
    @ResponseBody
    @PostMapping("/room")
    public ResponseEntity<RoomResponse> createRoom(RoomRequest request) {
        return ResponseEntity.ok(roomService.createRoom(request));
    }

    // WebSocket 엔드포인트 - 방 정보 조회
    @MessageMapping("/room/info")
    public void getRoomParticipants(RoomInfoRequest request) {
        RoomInfoResponse response = roomService.getRoomInfoByRoomCode(request);
        messagingTemplate.convertAndSend(
                "/topic/room/" + request.getRoomCode(),
                response
        );
    }

    // WebSocket 엔드포인트 - 방 참여
    @MessageMapping("/room/join")
    public void joinRoom(RoomJoinRequest request, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocketConfig 에서 저장한 memberId를 세션에서 가져옴
        Long memberId = (Long) headerAccessor.getSessionAttributes().get("memberId");

        roomService.joinRoom(request, memberId);
    }

    // WebSocket 엔드포인트 - 방 떠나기
    @MessageMapping("/room/leave")
    public void leaveRoom(RoomLeaveRequest request, SimpMessageHeaderAccessor headerAccessor) {
        // WebSocketConfig 에서 저장한 memberId를 세션에서 가져옴
        Long memberId = (Long) headerAccessor.getSessionAttributes().get("memberId");
        // RoomService 호출 시 roomId와 세션에서 가져온 memberId 사용
        roomService.leaveRoom(request, memberId);
    }


}