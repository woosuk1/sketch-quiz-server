package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    private Integer id; // 게임 ID
    private Room room; // 게임이 속한 방
    private Turn currentTurnId; // 현재 턴
    private LocalDateTime startedAt; // 시작 시간
    private LocalDateTime endedAt; // 종료 시간

    public enum GameStatus {
        NOT_STARTED, IN_PROGRESS, ENDED
    }
}
