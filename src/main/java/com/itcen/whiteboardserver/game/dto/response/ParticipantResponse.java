package com.itcen.whiteboardserver.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private Long memberId;
    private String memberName;
    private boolean isHost;
}