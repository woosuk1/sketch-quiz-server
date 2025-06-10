package com.itcen.whiteboardserver.member.dto;

import com.itcen.whiteboardserver.member.enums.ProfileColor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDTO {
    @Schema(description = "회원 ID", example = "1")
    private Long id;
    @Schema(description = "회원 이름", example = "용감한 풍뎅이")
    private String nickname;
    @Schema(description = "회원 email", example = "aa@gmail.com")
    private String email;
    @Schema(description = "프로필 색상", example = "ORANGE")
    private ProfileColor profileColor;

}
