package com.itcen.whiteboardserver.member.service;

import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;

public interface MemberService {
    // Define the methods that will be implemented in the service class
    MemberDTO getMemberById(Long id);

    String getRandomNickname();

    MemberDTO postChangeNickname(CustomPrincipal principal);

    MemberDTO getMemberByEmail(String email);
}
