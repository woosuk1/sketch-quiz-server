package com.itcen.whiteboardserver.member.service;

import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.dto.MemberResponseDTO;
import com.itcen.whiteboardserver.member.dto.NicknameDTO;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;

public interface MemberService {

    NicknameDTO postRandomNickname();

    MemberDTO getMemberByEmail(String email);

    NicknameDTO getRandomNickname();

    MemberResponseDTO patchNickname(CustomPrincipal principal, NicknameDTO nicknameDTO);
}
