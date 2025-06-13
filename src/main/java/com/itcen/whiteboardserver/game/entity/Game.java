package com.itcen.whiteboardserver.game.entity;

import com.itcen.whiteboardserver.turn.entitiy.Turn;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게임 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_turn_id")
    private Turn currentTurn; // 현재 턴

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Version
    private int version;

    public Game(Long id, Turn currentTurn, GameStatus status) {
        this.id = id;
        this.currentTurn = currentTurn;
        this.status = status;
    }

    public enum GameStatus {
        NOT_STARTED, IN_PROGRESS, ENDED
    }

    public void changeTurn(Turn turn) {
        currentTurn = turn;
    }

    public void thisTurnDown() {
        currentTurn = null;
    }

    public void quitGame() {
        status = GameStatus.ENDED;
    }
}
