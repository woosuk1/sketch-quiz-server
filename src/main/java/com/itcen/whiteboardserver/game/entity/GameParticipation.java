package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameParticipation {
    @Id
    private Integer id; // 게임 참여 결과 ID
    private Game game; // 게임
    private Member member; // 사용자
    private Integer score; // 점수
}
