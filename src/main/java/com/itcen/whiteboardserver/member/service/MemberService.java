package com.itcen.whiteboardserver.member.service;

import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.dto.NicknameDTO;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;

public interface MemberService {

    NicknameDTO getRandomNickname();

    MemberDTO postChangeNickname(CustomPrincipal principal);

    MemberDTO getMemberByEmail(String email);
}
