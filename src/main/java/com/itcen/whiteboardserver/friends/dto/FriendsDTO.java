package com.itcen.whiteboardserver.friends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
//@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendsDTO {
    @Schema(description = "친구 ID", example = "1")
    private Long friend_id;
    @Schema(description = "친구 이름", example = "용감한 풍뎅이")
    private String friend_nickname;
    @Schema(description = "회원 이름", example = "당당한 풍뎅이")
    private String member_nickname;
    @Schema(description = "회원 번호", example = "1")
    private String member_id;


    // 추가적인 필드나 메서드가 필요할 경우 여기에 작성
}
