package com.itcen.whiteboardserver.turn.dto.response.data;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record TurnData(
        Long turnId,
        Long drawerId,
        LocalDateTime startTime,
        LocalDateTime endTime
) {
}
