package com.itcen.whiteboardserver.turn.dto.response.data;

import java.util.List;

public record TurnQuitData(
        Long gameId,
        List<MemberScore> members
) {
}


