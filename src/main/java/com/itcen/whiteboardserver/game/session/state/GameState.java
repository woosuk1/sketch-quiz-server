package com.itcen.whiteboardserver.game.session.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GameState {
    private List<Long> drawerSequence;
    private List<String> quizWords;
    private int totalTurnCnt;
    private int nowTurn;
    private GameStatus status;
    private static final int TURN = 2;


    public static GameState createGameState(List<Long> drawerSequence, List<String> quizWords) {
        int drawerCnt = drawerSequence.size();
        if (drawerCnt < 2 || drawerCnt > 6) {
            throw new RuntimeException("Invalid Game Participants: " + (drawerCnt < 2 ? "MIN" : "MAX"));
        }

        Set<Long> drawerSet = new HashSet<>(drawerSequence);
        if (drawerSet.size() != drawerCnt) {
            throw new RuntimeException("Duplicated Members in sequence!");
        }

        return new GameState(
                List.copyOf(drawerSequence),
                List.copyOf(quizWords),
                TURN * drawerCnt,
                -1,
                GameStatus.NOT_STARTED
        );
    }

    public List<Long> getDrawerSequence() {
        return List.copyOf(drawerSequence);
    }

    public String getThisTurnWord() {
        return quizWords.get(nowTurn);
    }

    public void goNextTurn() {
        if (nowTurn >= totalTurnCnt) {
            throw new RuntimeException("턴 증가가 전체 턴을 넘었습니다.");
        }

        nowTurn++;
        status = GameStatus.IN_TURN;
    }

    public void endTurn() {
        status = GameStatus.BETWEEN_TURNS;
    }
}
