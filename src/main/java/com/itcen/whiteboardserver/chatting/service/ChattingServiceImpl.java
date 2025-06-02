package com.itcen.whiteboardserver.chatting.service;

import com.itcen.whiteboardserver.chatting.dto.ChattingRequest;
import com.itcen.whiteboardserver.game.session.GameSession;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponse;
import com.itcen.whiteboardserver.turn.dto.response.TurnResponseType;
import com.itcen.whiteboardserver.turn.service.TurnService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChattingServiceImpl implements ChattingService {

    private final GameSession gameSession;
    private final MemberRepository memberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final TurnService turnService;

    @Override
    public void chat(ChattingRequest chattingRequest) {
        if (!gameSession.isGamePlaying(chattingRequest.gameId())) {
            broadcastChat(chattingRequest.gameId(), chattingRequest.message());
            return;
        }

        Long memberId = getMemberIdFromEmail(chattingRequest.email());
        if (!gameSession.isThisMemberParticipant(chattingRequest.gameId(), memberId)) {
            throw new RuntimeException("채팅 요청한 회원이 게임 참가자가 아닙니다.");
        }

        if (gameSession.isThisDrawer(chattingRequest.gameId(), memberId)) {
            throw new RuntimeException("채팅 요청한 회원이 출제자이기 때문에 채팅이 금지됩니다.");
        }

        if(turnService.isAlreadyCorrect(chattingRequest.gameId(), memberId)){
            throw new RuntimeException("채팅 요청한 회원이 이미 정답을 맞춰 채팅이 금지됩니다.");
        }

        String answer = gameSession.getNowTurnQuizWord(chattingRequest.gameId());
        String message = chattingRequest.message();
        System.out.println(answer + ", " + message);
        if (answer.replaceAll(" ", "").equals(message.replaceAll(" ", ""))) {
            message = chattingRequest.email() + "님이 정답을 맞추셨습니다.";

            turnService.correct(chattingRequest.gameId(), memberId);

            //TODO: 모든 사람이 다 정답 맞췄는지 확인 -> 종료
        }

        broadcastChat(chattingRequest.gameId(), message);
    }

    private void broadcastChat(Long gameId, String message) {
        TurnResponse<String> response = new TurnResponse(
                TurnResponseType.CHAT,
                message
        );

        messagingTemplate.convertAndSend("/topic/game/" + gameId + "/chat", response);
    }

    private Long getMemberIdFromEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("해당하는 이메일의 회원이 존재하지 않습니다.")
        ).getId();
    }
}
