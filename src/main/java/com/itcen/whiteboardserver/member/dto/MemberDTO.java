package com.itcen.whiteboardserver.member.dto;

import com.itcen.whiteboardserver.member.enums.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    @Schema(description = "회원 ID", example = "1")
    private Long id;
    @Schema(description = "회원 이름", example = "용감한 풍뎅이")
    private String nickname;
    @Schema(description = "회원 email", example = "aa@gmail.com")
    private String email;
    @Schema(description = "회원 역할", example = "MEMBER")
    private Set<MemberRole> memberRole;
}
