package com.itcen.whiteboardserver.game.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomHostChangedEvent {
    private Long roomCode;
    private Long newHostId;
}
