package com.itcen.whiteboardserver.member.service;

import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.member.repository.NicknamesRepository;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final NicknamesRepository nicknamesRepository;

    @Override
    public MemberDTO getMemberById(Long id) {

        return memberRepository.findById(id)
                .map(member -> new MemberDTO(member.getId(), member.getNickname(), member.getEmail(), member.getMemberRole()))
                .orElse(null);
    }

    @Override
    public MemberDTO getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> new MemberDTO(member.getId(), member.getNickname(), member.getEmail() , member.getMemberRole()))
                .orElse(null);
    }


    public String getRandomNickname() {
        return nicknamesRepository.findRandomNickname();
    }

    @Override
    @Transactional
    public MemberDTO postChangeNickname(CustomPrincipal principal) {

        /* 설명. 닉네임 repo에서 닉네임 가져오기
         *  1. is_used가 false인 닉네임을 가져온다.
         *  2. 가져온 닉네임을 사용자의 닉네임으로 설정한다.
         *  3. 사용한 닉네임은 is_used를 true로 변경한다.
        * */

        String randomNickname = getRandomNickname();
        if (randomNickname != null) {
//            Long memberId = 1L; // TODO: 현재 로그인한 사용자의 ID를 가져오는 로직으로 변경해야 함
//            // 실제로는 principal.getId()를 사용하여 현재 로그인한 사용자의 ID를 가져와야 합니다.
//
//            memberRepository.updateNickname(memberId, randomNickname);
//            return getMemberById(memberId);
        }
        return null;
    }

}
