package com.itcen.whiteboardserver.draw.controller;

import com.itcen.whiteboardserver.draw.dto.DrawDto;
import com.itcen.whiteboardserver.draw.service.DrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class DrawController {
    private final DrawService drawService;

    @MessageMapping("/game/{gameId}/draw")
    public void draw(@DestinationVariable Long gameId, DrawDto drawDto, Principal principal) {
        String email = principal.getName();

        drawService.draw(drawDto, gameId, email);
    }
}
