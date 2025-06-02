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

    //TODO: Exception 반환형 추후 고려
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
                3 * drawerCnt,
                -1
        );
    }

    public List<Long> getDrawerSequence() {
        return List.copyOf(drawerSequence);
    }

    public List<String> getQuizWords() {
        return List.copyOf(quizWords);
    }

    public String getThisTurnWord() {
        return quizWords.get(nowTurn);
    }

    public void increaseNowTurn() {
        if (nowTurn >= totalTurnCnt) {
            throw new RuntimeException("턴 증가가 전체 턴을 넘었습니다.");
        }

        nowTurn++;
    }
}
