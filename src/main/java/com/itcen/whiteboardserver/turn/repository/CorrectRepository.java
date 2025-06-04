package com.itcen.whiteboardserver.turn.repository;

import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.turn.entitiy.Correct;
import com.itcen.whiteboardserver.turn.entitiy.Turn;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CorrectRepository extends JpaRepository<Correct, Long> {
    boolean existsByTurnAndMember(Turn turn, Member member);

    List<Correct> findAllByTurn(Turn turn);
    List<Correct> findAllByTurnOrderByCreatedAtDesc(Turn turn);
}
