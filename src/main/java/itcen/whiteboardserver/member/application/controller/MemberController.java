package itcen.whiteboardserver.member.application.controller;

import itcen.whiteboardserver.member.application.dto.MemberDTO;
import itcen.whiteboardserver.member.application.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/member")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMember(@PathVariable("id") Long id) {
        // Logic to retrieve member by ID
        MemberDTO member = memberService.getMemberById(id);
        return ResponseEntity.ok()
                .body(Map.of(
                        "msg", "성공",
                        "result", member,
                        "status", HttpStatus.OK
                ));

    }
}
