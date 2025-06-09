package com.itcen.whiteboardserver.game.dto.response;

import com.itcen.whiteboardserver.member.enums.ProfileColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipantResponse {
    private Long memberId;
    private String memberName;
    private boolean isHost;
    private ProfileColor profileColor;
}