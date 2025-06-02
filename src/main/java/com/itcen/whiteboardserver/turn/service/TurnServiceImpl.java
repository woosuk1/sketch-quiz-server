package com.itcen.whiteboardserver.turn.service;

import com.itcen.whiteboardserver.game.entity.Game;
import com.itcen.whiteboardserver.game.repository.GameRepository;
import com.itcen.whiteboardserver.game.session.GameSession;
import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponse;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponseType;
import com.itcen.whiteboardserver.turn.dto.response.data.TurnData;
import com.itcen.whiteboardserver.turn.entitiy.Correct;
import com.itcen.whiteboardserver.turn.entitiy.Turn;
import com.itcen.whiteboardserver.turn.mapper.TurnMapper;
import com.itcen.whiteboardserver.turn.repository.CorrectRepository;
import com.itcen.whiteboardserver.turn.repository.TurnRepository;
import jakarta.transaction.Transactional;
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
    final CorrectRepository correctRepository;


    @Override
    public void startTurn(Long gameId) {
        if (!gameSession.canGoNextTurn(gameId)) {
            //TODO: 게임 종료
        }

        Turn turn = createTurn(gameId);
        turnRepository.save(turn);
        Game game = getGameByGameId(gameId);
        game.changeTurn(turn);
        gameRepository.save(game);

        broadcastTurnInfo(gameId, turn);
        sendDrawInfoToDrawer(gameId, turn);

        //TODO: 2분 30초 후 스케줄링
    }

    @Transactional
    @Override
    public void correct(Long gameId, Long memberId) {
        Game game = getGameByGameId(gameId);
        Turn turn = game.getCurrentTurn();

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new RuntimeException("해당하는 member가 존재하지 않습니다.")
        );

        Correct correct = Correct.builder()
                .turn(turn)
                .member(member)
                .build();

        correctRepository.save(correct);
    }

    @Transactional
    @Override
    public boolean isAlreadyCorrect(Long gameId, Long memberId) {
        Game game = getGameByGameId(gameId);
        Turn turn = game.getCurrentTurn();
        Member member = getMemberByMemberId(memberId);

        return correctRepository.existsByTurnAndMember(turn, member);
    }

    private void broadcastTurnInfo(Long gameId, Turn turn) {
        TurnResponse<TurnData> response = TurnMapper.turnToTurnDataResponse(turn);

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
    }

    private void sendDrawInfoToDrawer(Long gameId, Turn turn) {
        TurnResponse<String> response = new TurnResponse(
                TurnResponseType.DRAWER,
                turn.getQuizWord()
        );

        messagingTemplate.convertAndSendToUser(
                turn.getMember().getEmail(),
                "/topic/game/" + gameId,
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
