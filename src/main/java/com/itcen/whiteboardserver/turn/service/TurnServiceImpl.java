package com.itcen.whiteboardserver.turn.service;

import com.itcen.whiteboardserver.game.entity.Game;
import com.itcen.whiteboardserver.game.entity.GameParticipation;
import com.itcen.whiteboardserver.game.repository.GameParticipationRepository;
import com.itcen.whiteboardserver.game.repository.GameRepository;
import com.itcen.whiteboardserver.game.session.GameSession;
import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponse;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponseType;
import com.itcen.whiteboardserver.turn.dto.response.data.*;
import com.itcen.whiteboardserver.turn.entitiy.Correct;
import com.itcen.whiteboardserver.turn.entitiy.Turn;
import com.itcen.whiteboardserver.turn.mapper.TurnMapper;
import com.itcen.whiteboardserver.turn.repository.CorrectRepository;
import com.itcen.whiteboardserver.turn.repository.TurnRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Transactional
@RequiredArgsConstructor
@Service
public class TurnServiceImpl implements TurnService {

    final SimpMessagingTemplate messagingTemplate;
    final GameSession gameSession;
    final MemberRepository memberRepository;
    final GameRepository gameRepository;
    final TurnRepository turnRepository;
    final CorrectRepository correctRepository;
    final GameParticipationRepository gameParticipationRepository;
    final int TURN_SECONDS = 10;
    final PlatformTransactionManager transactionManager;
    final ApplicationContext applicationContext;
    final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    @Override
    public void startTurn(Long gameId) {
        if (!gameSession.canGoNextTurn(gameId)) {
            quitGame(gameId);

            return;
        }

        Game game = getGameByGameId(gameId);
        if (game.getCurrentTurn() != null && !game.getCurrentTurn().getIsTurnOver()) {
            throw new RuntimeException("현재 게임의 턴이 끝나지 않았습니다.");
        }

        Turn turn = createTurn(gameId);
        turnRepository.save(turn);

        game.changeTurn(turn);
        gameRepository.save(game);

        broadcastTurnInfo(gameId, turn);
        sendDrawInfoToDrawer(gameId, turn);

        scheduleTurnOver(turn.getId());
    }

    private void scheduleTurnOver(Long turnId) {
        scheduler.schedule(() -> {
            TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
            txTemplate.execute(status -> {

                Turn freshTurn = turnRepository.findById(turnId).orElseThrow();
                if (freshTurn.getIsTurnOver()) {
                    return null;
                }

                Game game = freshTurn.getGame();
                if (!game.getCurrentTurn().getId().equals(freshTurn.getId())) {
                    throw new RuntimeException("게임의 현재 턴과 일치하지 않음");
                }

                doTurnOver(game.getId());

                return null;
            });
        }, TURN_SECONDS, TimeUnit.SECONDS);
    }

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
        broadcastCorrect(
                new CorrectData(
                        memberId,
                        turn.getId(),
                        game.getId()
                )
        );
    }

    @Transactional
    private void quitGame(Long gameId) {
        broadcastTurnScore(gameId, TurnResponseType.GAME_FINISH);

        gameSession.removeSession(gameId);
        Game game = getGameByGameId(gameId);

        game.changeTurn(null);
        game.quitGame();

        gameRepository.save(game);
    }

    private void broadcastCorrect(CorrectData correctData) {
        TurnResponse<CorrectData> response = new TurnResponse(
                TurnResponseType.CORRECT,
                correctData
        );

        messagingTemplate.convertAndSend("/topic/game/" + correctData.gameId(), response);
    }

    @Override
    public boolean isAlreadyCorrect(Long gameId, Long memberId) {
        Game game = getGameByGameId(gameId);
        Turn turn = game.getCurrentTurn();
        Member member = getMemberByMemberId(memberId);

        return correctRepository.existsByTurnAndMember(turn, member);
    }

    @Override
    public boolean turnOverIfPossible(Long gameId) {
        Game game = getGameByGameId(gameId);
        Turn turn = game.getCurrentTurn();

        if (turn.getIsTurnOver()) {
            throw new RuntimeException("이미 끝난 턴입니다.");
        }

        List<Correct> corrects = correctRepository.findAllByTurn(turn);
        List<GameParticipation> participations = gameParticipationRepository.findAllByGame(game);

        if (participations.size() - 1 != corrects.size()) {
            return false;
        }

        doTurnOver(gameId);

        return true;
    }

    @Transactional
    private void doTurnOver(Long gameId) {
        Game game = getGameByGameId(gameId);
        Turn turn = game.getCurrentTurn();

        if (turn.getIsTurnOver()) {
            throw new RuntimeException("이미 끝난 턴입니다.");
        }

        finalizeTurnScore(gameId);

        game.thisTurnDown();
        gameRepository.save(game);
        turn.doTurnOver();
        turnRepository.save(turn);

        applicationContext.getBean(TurnService.class).startTurn(gameId);
    }

    @Transactional
    private void finalizeTurnScore(Long gameId) {
        Game game = getGameByGameId(gameId);
        Turn turn = game.getCurrentTurn();

        //정답자 점수
        List<Correct> corrects = correctRepository.findAllByTurnOrderByCreatedAtDesc(turn);
        int score = 10;
        for (Correct correct : corrects) {
            GameParticipation gameParticipation = gameParticipationRepository.findByGameAndMember(game, correct.getMember())
                    .orElseThrow(
                            () -> new RuntimeException("게임, 회원에 해당하는 게임 참가이력이 없습니다.")
                    );

            gameParticipation.increaseScore(score);
            score += 10;
        }

        //출제자 점수
        int participationCnt = gameParticipationRepository.countByGame(game);
        Member drawer = turn.getMember();

        if (participationCnt - 1 == corrects.size()) {
            int drawerScore = participationCnt / 2 * 10;

            GameParticipation gameParticipation = gameParticipationRepository.findByGameAndMember(game, drawer).orElseThrow(
                    () -> new RuntimeException("현재 게임, 출제자에 해당하는 게임 참여가 없습니다.")
            );

            gameParticipation.increaseScore(drawerScore);
        }

        broadcastTurnScore(gameId, TurnResponseType.FINISH);
    }

    @Transactional
    private void broadcastTurnScore(Long gameId, TurnResponseType type) {
        Game game = getGameByGameId(gameId);

        List<GameParticipation> gameParticipations = gameParticipationRepository.findAllByGame(game);

        List<MemberScore> memberScores = new ArrayList<>();
        for (GameParticipation gameParticipation : gameParticipations) {
            memberScores.add(
                    new MemberScore(gameParticipation.getMember().getId(), gameParticipation.getScore())
            );
        }

        TurnResponse<TurnQuitData> response = new TurnResponse<>(
                type,
                new TurnQuitData(
                        gameId,
                        memberScores
                )
        );

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
    }

    private void broadcastTurnInfo(Long gameId, Turn turn) {
        TurnResponse<TurnData> response = TurnMapper.turnToTurnDataResponse(turn);

        messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
    }

    private void sendDrawInfoToDrawer(Long gameId, Turn turn) {
        TurnResponse<DrawerData> response = new TurnResponse(
                TurnResponseType.DRAWER,
                new DrawerData(
                        turn.getQuizWord(),
                        turn.getId()
                )
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
        return gameRepository.findById(gameId).orElseThrow(
                () -> new RuntimeException("현재 gameId의 게임이 없습니다.")
        );
    }
}
