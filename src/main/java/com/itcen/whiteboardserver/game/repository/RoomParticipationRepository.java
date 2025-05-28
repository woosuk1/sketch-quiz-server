package com.itcen.whiteboardserver.game.repository;

import com.itcen.whiteboardserver.game.entity.RoomParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipationRepository extends JpaRepository<RoomParticipation, Long> {
    List<RoomParticipation> findByRoomId(Long roomId);

    Optional<RoomParticipation> findByRoomIdAndMemberId(Long roomId, Long memberId);
}
