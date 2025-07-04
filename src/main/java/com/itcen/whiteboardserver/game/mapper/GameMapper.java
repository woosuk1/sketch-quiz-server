package com.itcen.whiteboardserver.game.mapper;

import com.itcen.whiteboardserver.game.dto.response.GameParticipantResponse;
import com.itcen.whiteboardserver.member.entity.Member;

public class GameMapper {

    public static GameParticipantResponse memberToGameParticipantResponse(Member member) {
        return GameParticipantResponse.builder()
                .memberId(member.getId())
                .nickName(member.getNickname())
                .score(0)
                .profileColor(member.getProfileColor())
                .build();
    }
}
