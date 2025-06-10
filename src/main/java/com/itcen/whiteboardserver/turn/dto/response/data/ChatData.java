package com.itcen.whiteboardserver.turn.dto.response.data;

public record ChatData(
        String message,
        Long memberId,
        String nickName
) {
}
