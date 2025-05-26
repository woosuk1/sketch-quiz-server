package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    private Integer id; // 방 ID
    private String code; // 방 코드
    private String title; // 방 제목
    private Member ownerId; // 방장 사용자 ID
    private Integer maxPlayerCnt; // 최대 플레이어 수
    private RoomStatus status; // WAITING, PLAYING, FINISHED
    private LocalDateTime createdAt; // 생성 시간

    public enum RoomStatus {
        WAITING, PLAYING, FINISHED
    }

}
