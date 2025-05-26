package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TurnAnswer {
    @Id
    private Integer id; // 해당 턴에 정답 ID
    private Turn turn; // 소속된 턴
    private Member member; // 정답자
    private LocalDateTime createdAt; // 생성 시간
}
