package com.itcen.whiteboardserver.turn.entitiy;

import com.itcen.whiteboardserver.game.entity.Game;
import com.itcen.whiteboardserver.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "turn")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Turn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Game game;

    @Column(nullable = false, length = 30)
    private String quizWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isTurnOver = false;

    public void doTurnOver() {
        isTurnOver = true;
    }
}
