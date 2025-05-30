package com.itcen.whiteboardserver.member.controller;

import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> getMember(@AuthenticationPrincipal CustomPrincipal principal) {
//        MemberDTO member = memberService.getMemberById(id);

        return ResponseEntity.ok()
                .body(principal.getEmail());
    }
}
