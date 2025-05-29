package com.itcen.whiteboardserver.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfoResponse {
    private List<ParticipantResponse> participantList;
    private Long roomCode;
}
