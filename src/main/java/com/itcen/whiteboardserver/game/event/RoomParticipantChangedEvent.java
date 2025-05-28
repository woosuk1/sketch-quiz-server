package com.itcen.whiteboardserver.game.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomParticipantChangedEvent {
    private Long roomCode;
}

