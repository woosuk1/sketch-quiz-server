package com.itcen.whiteboardserver.game.entity;

import com.itcen.whiteboardserver.member.domain.aggregate.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 방 ID

    @Column(nullable = false)
    private String title; // 방 제목

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private Member hostId; // 방장 사용자 ID

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_game_id")
    private Game currentGame;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomStatus status; // WAITING, PLAYING, FINISHED

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 시간

    public enum RoomStatus {
        WAITING, PLAYING, FINISHED
    }

}
