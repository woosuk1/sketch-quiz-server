package com.itcen.whiteboardserver.game.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "game")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 게임 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room; // 게임이 속한 방

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_turn_id")
    private Turn currentTurnId; // 현재 턴

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime startedAt; // 시작 시간

    private LocalDateTime endedAt; // 종료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    public enum GameStatus {
        NOT_STARTED, IN_PROGRESS, ENDED
    }
}
