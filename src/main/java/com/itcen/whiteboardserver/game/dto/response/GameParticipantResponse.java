package com.itcen.whiteboardserver.game.dto.response;

import lombok.Builder;

@Builder
public record GameParticipantResponse(
        Long memberId,
        String nickName,
        int score
) {
}
