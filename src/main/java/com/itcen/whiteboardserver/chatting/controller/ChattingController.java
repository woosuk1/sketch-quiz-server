package com.itcen.whiteboardserver.chatting.controller;

import com.itcen.whiteboardserver.chatting.dto.ChattingRequest;
import com.itcen.whiteboardserver.chatting.service.ChattingService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChattingController {

    private final ChattingService chattingService;

    @MessageMapping("/game/{gameId}/chat")
    public void chat(@DestinationVariable Long gameId, String chat, Principal principal) {
        String email = principal.getName();

        chattingService.chat(new ChattingRequest(gameId, email, chat));
    }
}
