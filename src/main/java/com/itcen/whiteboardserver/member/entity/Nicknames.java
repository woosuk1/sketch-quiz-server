package com.itcen.whiteboardserver.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nicknames")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Nicknames {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(name = "is_used", nullable = false, columnDefinition = "boolean default false")
    private boolean isUsed;
}
