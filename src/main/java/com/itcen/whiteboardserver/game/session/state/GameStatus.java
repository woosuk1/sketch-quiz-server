package com.itcen.whiteboardserver.game.session.state;

public enum GameStatus {
    NOT_STARTED,   // 게임 시작 전
    IN_TURN,       // 턴 진행 중
    BETWEEN_TURNS, // 턴 사이의 대기 시간
    ENDED          // 게임 종료
}