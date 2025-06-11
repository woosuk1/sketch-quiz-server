package com.itcen.whiteboardserver.game.repository;

import com.itcen.whiteboardserver.game.entity.Game;
import com.itcen.whiteboardserver.game.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByCurrentGame(Game game);
}
