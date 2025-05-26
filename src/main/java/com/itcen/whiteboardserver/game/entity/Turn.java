package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Turn {
    @Id
    private Integer id; // 턴 ID
    private Game game; // 소속된 게임
    private String keyword; // 그림 키워드
    private Member questionerId; // 그림 그리는 사람
    private LocalDateTime createdAt; // 생성 시간
}
