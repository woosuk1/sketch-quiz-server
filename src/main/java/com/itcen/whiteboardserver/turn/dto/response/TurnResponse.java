package com.itcen.whiteboardserver.turn.dto.response;


public record TurnResponse<T>(TurnResponseType type, T data) {
}
