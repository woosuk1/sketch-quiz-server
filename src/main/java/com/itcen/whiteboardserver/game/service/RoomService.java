package com.itcen.whiteboardserver.game.service;

import com.itcen.whiteboardserver.game.dto.request.RoomInfoRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomJoinRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomLeaveRequest;
import com.itcen.whiteboardserver.game.dto.request.RoomRequest;
import com.itcen.whiteboardserver.game.dto.response.ParticipantResponse;
import com.itcen.whiteboardserver.game.dto.response.RoomInfoResponse;
import com.itcen.whiteboardserver.game.dto.response.RoomResponse;
import com.itcen.whiteboardserver.game.entity.Room;
import com.itcen.whiteboardserver.game.entity.RoomParticipation;
import com.itcen.whiteboardserver.game.event.RoomParticipantChangedEvent;
import com.itcen.whiteboardserver.game.repository.RoomParticipationRepository;
import com.itcen.whiteboardserver.game.repository.RoomRepository;
import com.itcen.whiteboardserver.member.domain.aggregate.entity.Member;
import com.itcen.whiteboardserver.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomParticipationRepository participationRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    /**
     * 방 생성
     */
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        Member host = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 방 생성
        Room room = new Room(
                null,
                null,
                host,
                null,
                Room.RoomStatus.WAITING,
                null
        );
        Room savedRoom = roomRepository.save(room);

        // 참가자로 등록
        RoomParticipation participation = new RoomParticipation(null, savedRoom, host);
        participationRepository.save(participation);

        // 방 코드 반환
        return new RoomResponse(room.getId());
    }

    /**
     * 방 정보 조회
     */
    @Transactional(readOnly = true)
    public RoomInfoResponse getRoomInfoByRoomCode(RoomInfoRequest request) {
        // 방 정보
        Room room = roomRepository.findById(request.getRoomCode())
                .orElseThrow(() -> new RuntimeException("방을 찾을 수 없습니다."));

        // 참여자 정보
        List<RoomParticipation> roomParticipationList = participationRepository.findByRoomId(request.getRoomCode());
        List<ParticipantResponse> participantList = roomParticipationList.stream()
                .map(roomParticipation -> {
                    Member member = roomParticipation.getMember();
                    boolean isHost = Objects.equals(member.getId(), room.getHost().getId());
                    return new ParticipantResponse(member.getId(), member.getName(), isHost);
                })
                .toList();

        return new RoomInfoResponse(participantList, room.getId());
    }

    /**
     * 방 참여
     */
    @Transactional
    public void joinRoom(RoomJoinRequest request, Long memberId) {
        // 방
        Long roomCode = request.getRoomCode();
        Room room = roomRepository.findById(roomCode)
                .orElseThrow(() -> new RuntimeException("방을 찾을 수 없습니다."));
        if (room.getStatus() != Room.RoomStatus.WAITING) {
            throw new RuntimeException("참여할 수 없는 방입니다.");
        }

        // 사용자
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 참여 중인지 확인
        boolean alreadyJoined = participationRepository
                .findByRoomIdAndMemberId(room.getId(), member.getId())
                .isPresent();
        if (alreadyJoined) {
            throw new RuntimeException("이미 참여중인 방입니다.");
        }

        // 참가자로 등록
        RoomParticipation participation = new RoomParticipation(null, room, member);
        participationRepository.save(participation);

        // 이벤트 발행
        applicationEventPublisher.publishEvent(new RoomParticipantChangedEvent(roomCode));
    }

    /**
     * 방 떠나기
     */
    @Transactional
    public void leaveRoom(RoomLeaveRequest request, Long memberId) {

        // 룸
        Long roomId = request.getRoomId();
        Optional<RoomParticipation> participation = participationRepository.findByRoomIdAndMemberId(roomId, memberId);

        if (participation.isPresent()) {
            // 방 참여 정보 삭제
            RoomParticipation roomParticipation = participation.get();
            participationRepository.delete(roomParticipation);

            // 만약 방장이 나가는 경우, 방의 상태를 변경하거나 다른 사용자를 방장으로 변경하는 로직
            Room room = roomParticipation.getRoom();
            if (Objects.equals(room.getHost().getId(), memberId)) {
                List<RoomParticipation> remainingParticipants = participationRepository.findByRoomId(room.getId());

                if (remainingParticipants.isEmpty()) {
                    // 참가자가 없으면 방 상태 변경
                    room.updateStatus(Room.RoomStatus.FINISHED);
                    roomRepository.save(room);
                } else {
                    // 첫 번째 참가자를 새 방장으로 지정
                    Member newHost = remainingParticipants.get(0).getMember();
                    room.updateHost(newHost);
                    roomRepository.save(room);

                    // 참가자 변경 이벤트 발행
                    applicationEventPublisher.publishEvent(new RoomParticipantChangedEvent(roomId));
                }
            }
        }
    }


}