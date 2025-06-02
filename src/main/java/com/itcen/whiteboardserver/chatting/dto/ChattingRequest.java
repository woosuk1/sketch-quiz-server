package com.itcen.whiteboardserver.chatting.dto;

public record ChattingRequest(
        Long gameId,
        String email,
        String message
) {
}
