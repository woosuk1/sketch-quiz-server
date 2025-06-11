package com.itcen.whiteboardserver.game.dto.response;

import com.itcen.whiteboardserver.member.enums.ProfileColor;
import lombok.Builder;

@Builder
public record GameParticipantResponse(
        Long memberId,
        String nickName,
        int score,
        ProfileColor profileColor
) {
}
