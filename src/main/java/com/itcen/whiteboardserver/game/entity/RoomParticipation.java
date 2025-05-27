package com.itcen.whiteboardserver.game.entity;

import com.itcen.whiteboardserver.member.domain.aggregate.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "room_participation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"room_id", "member_id"})
        },
        indexes = {
                @Index(columnList = "room_id, member_id")
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 방 참여 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room; // 방

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member; // 사용자
}