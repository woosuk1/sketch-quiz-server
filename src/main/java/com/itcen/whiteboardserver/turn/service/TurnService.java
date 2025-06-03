package com.itcen.whiteboardserver.turn.service;

public interface TurnService {
    void startTurn(Long gameId);

    void correct(Long gameId, Long memberId);

    boolean isAlreadyCorrect(Long gameId, Long memberId);

    boolean turnOverIfPossible(Long gameId);
}
