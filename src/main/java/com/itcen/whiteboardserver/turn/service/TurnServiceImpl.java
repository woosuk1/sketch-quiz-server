package com.itcen.whiteboardserver.turn.service;

import com.itcen.whiteboardserver.game.entity.Game;
import com.itcen.whiteboardserver.game.repository.GameRepository;
import com.itcen.whiteboardserver.game.session.GameSession;
import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponse;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponseType;
import com.itcen.whiteboardserver.turn.dto.response.data.TurnData;
import com.itcen.whiteboardserver.turn.entitiy.Turn;
import com.itcen.whiteboardserver.turn.mapper.TurnMapper;
import com.itcen.whiteboardserver.turn.repository.TurnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class TurnServiceImpl implements TurnService {

    final SimpMessagingTemplate messagingTemplate;
    final GameSession gameSession;
    final MemberRepository memberRepository;
    final GameRepository gameRepository;
    final TurnRepository turnRepository;


    @Override
    public void startTurn(Long gameId) {
        if (!gameSession.canGoNextTurn(gameId)) {
            //TODO: 게임 종료
        }

        Turn turn = createTurn(gameId);
        turnRepository.save(turn);

        broadcastTurnInfo(gameId, turn);
        sendDrawInfoToDrawer(gameId, turn);

        //TODO: 2분 30초 후 스케줄링
    }

    private void broadcastTurnInfo(Long gameId, Turn turn) {
        TurnResponse<TurnData> response = TurnMapper.turnToTurnDataResponse(turn);

        messagingTemplate.convertAndSend("/topic/turn/" + gameId, response);
    }

    private void sendDrawInfoToDrawer(Long gameId, Turn turn) {
        TurnResponse<String> response = new TurnResponse(
                TurnResponseType.DRAWER,
                turn.getQuizWord()
        );

        messagingTemplate.convertAndSendToUser(
                turn.getMember().getEmail(),
                "/topic/turn/" + gameId,
                response
        );
    }

    private Turn createTurn(Long gameId) {
        Long drawerId = gameSession.goNextTurnAndGetDrawer(gameId);
        Game game = getGameByGameId(gameId);
        Member drawer = getMemberByMemberId(drawerId);
        String quizWord = gameSession.getNowTurnQuizWord(gameId);
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusSeconds(150);

        return Turn.builder()
                .game(game)
                .member(drawer)
                .quizWord(quizWord)
                .startTime(startTime)
                .endTime(endTime)
                .build();
    }

    private Member getMemberByMemberId(Long memberId) {
        return memberRepository.getReferenceById(memberId);
    }

    private Game getGameByGameId(Long gameId) {
        return gameRepository.getReferenceById(gameId);
    }
}
