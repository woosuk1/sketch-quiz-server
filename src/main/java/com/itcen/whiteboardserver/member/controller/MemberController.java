package com.itcen.whiteboardserver.member.controller;

import com.itcen.whiteboardserver.global.exception.GlobalCommonException;
import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("")
    @Operation(
            summary = "멤버 조회",
            description =
                    """
                    현재 로그인한 사용자의 정보를 가져옵니다.
                    """
    )
//    public ResponseEntity<String> getMember(@AuthenticationPrincipal CustomPrincipal principal) {
    public ResponseEntity<MemberDTO> getMember(@AuthenticationPrincipal CustomPrincipal principal) {
//        MemberDTO member = memberService.getMemberById(id);

        MemberDTO member = MemberDTO.builder()
                .id(principal.getId())
                .nickname(principal.getNickname())
                .email(principal.getEmail())
                .build();

        return ResponseEntity.ok()
                .body(member);
    }

    @GetMapping("/nickname")
    @Operation(
            summary = "닉네임 재설정",
            description =
                    """
                    현재 로그인한 사용자의 닉네임을 랜덤으로 재설정합니다.
                    """
    )
    public ResponseEntity<MemberDTO> postChangeNickname(@AuthenticationPrincipal CustomPrincipal principal) {
        MemberDTO member = memberService.postChangeNickname(principal);

        return ResponseEntity.ok()
                .body(member);
    }
}
