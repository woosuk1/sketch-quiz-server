package com.itcen.whiteboardserver.turn.repository;

import com.itcen.whiteboardserver.turn.entitiy.Turn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurnRepository extends JpaRepository<Turn, Long> {
}
