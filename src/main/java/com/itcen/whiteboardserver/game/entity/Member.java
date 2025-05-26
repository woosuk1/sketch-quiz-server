package com.itcen.whiteboardserver.game.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    @Id
    private Integer id;
    private LocalDateTime createdAt; // 생성 시간
}
