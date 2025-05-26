package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipation {
    @Id
    private Integer id; // 방 참여 ID
    private Room room; // 방
    private Member member; // 사용자
    private LocalDateTime deletedAt; // 없어진 시간
}
