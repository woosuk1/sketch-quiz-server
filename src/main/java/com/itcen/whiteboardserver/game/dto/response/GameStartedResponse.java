package com.itcen.whiteboardserver.game.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameStartedResponse {
    private Long gameId;
    private Long roomCode;
    private final String type = "GAME_STARTED";
    private List<GameParticipantResponse> gameParticipants;
}
