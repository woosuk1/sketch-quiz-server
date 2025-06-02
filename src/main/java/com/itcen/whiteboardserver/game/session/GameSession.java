package com.itcen.whiteboardserver.game.session;

import com.itcen.whiteboardserver.game.session.state.GameState;
import com.itcen.whiteboardserver.game.session.state.GameStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameSession {
    private final Map<Long, GameState> gameSession = new ConcurrentHashMap<>();

    //TODO: Exception 반환형 추후 고려
    public void createSession(Long gameId, GameState gameState) {
        if (gameSession.containsKey(gameId)) {
            throw new RuntimeException("Already this game session created");
        }

        gameSession.put(gameId, gameState);
    }

    public void removeSession(Long gameId) {
        validateGameExist(gameId);

        gameSession.remove(gameId);
    }

    public String thisTurnQuizWord(Long gameId) {
        GameState gameState = getGameState(gameId);

        return gameState.getQuizWords().get(gameState.getNowTurn());
    }

    public boolean canGoNextTurn(Long gameId) {
        GameState gameState = getGameState(gameId);
        return !(gameState.getNowTurn() + 1 >= gameState.getTotalTurnCnt());
    }

    public Long goNextTurnAndGetDrawer(Long gameId) {
        GameState gameState = getGameState(gameId);
        gameState.goNextTurn();

        int drawerId = gameState.getNowTurn() % gameState.getDrawerSequence().size();
        return gameState.getDrawerSequence().get(drawerId);
    }

    public String getNowTurnQuizWord(Long gameId) {
        GameState gameState = getGameState(gameId);

        return gameState.getThisTurnWord();
    }

    public boolean isGamePlaying(Long gameId) {
        GameState gameState = getGameState(gameId);
        GameStatus status = gameState.getStatus();

        return status == GameStatus.IN_TURN;
    }

    public boolean isThisMemberParticipant(Long gameId, Long memberId) {
        GameState gameState = getGameState(gameId);

        return gameState.getDrawerSequence().contains(memberId);
    }

    public boolean isThisDrawer(Long gameId, Long memberId) {
        GameState gameState = getGameState(gameId);

        int drawerIdx = gameState.getNowTurn() % gameState.getDrawerSequence().size();
        Long drawerId = gameState.getDrawerSequence().get(drawerIdx);

        return memberId == drawerId;
    }

    private GameState getGameState(Long gameId) {
        validateGameExist(gameId);

        return gameSession.get(gameId);
    }

    private void validateGameExist(Long gameId) {
        if (!gameSession.containsKey(gameId)) {
            throw new RuntimeException("현재 게임이 존재하지 않습니다.");
        }
    }
}
