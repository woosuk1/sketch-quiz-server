package com.itcen.whiteboardserver.turn.mapper;

import com.itcen.whiteboardserver.turn.dto.response.TurnResponse;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponseType;
import com.itcen.whiteboardserver.turn.dto.response.data.TurnData;
import com.itcen.whiteboardserver.turn.entitiy.Turn;

public class TurnMapper {
    public static TurnResponse<TurnData> turnToTurnDataResponse(Turn turn) {
        TurnData turnData = TurnData.builder()
                .turnId(turn.getId())
                .drawerId(turn.getMember().getId())
                .startTime(turn.getStartTime())
                .endTime(turn.getEndTime())
                .build();

        return new TurnResponse(
                TurnResponseType.TURN,
                turnData
        );
    }
}
