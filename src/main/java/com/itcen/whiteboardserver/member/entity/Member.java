package com.itcen.whiteboardserver.member.entity;

import com.itcen.whiteboardserver.member.enums.AuthProvider;
import com.itcen.whiteboardserver.member.enums.MemberRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;


@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
@Getter
//@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성 시간

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt; // 수정 시간

    @Column(name = "member_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<MemberRole> memberRole;

    @Column(name = "provider", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;
}
