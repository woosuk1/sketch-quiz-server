package com.itcen.whiteboardserver.member.service;

import com.itcen.whiteboardserver.global.exception.GlobalCommonException;
import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.dto.NicknameDTO;
import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.member.repository.NicknamesRepository;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final NicknamesRepository nicknamesRepository;

    @Override
    public MemberDTO getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(member -> new MemberDTO(member.getId(), member.getNickname(), member.getEmail() , member.getMemberRole()))
                .orElseThrow(() -> new GlobalCommonException(GlobalErrorCode.MEMBER_NOT_FOUND));
    }


    public NicknameDTO getRandomNickname() {

        /* 설명. 닉네임 repo에서 닉네임 가져오기
         *  1. is_used가 false인 닉네임을 가져온다.
         *  2. 가져온 닉네임을 사용자의 닉네임으로 설정한다.
         *  3. 사용한 닉네임은 is_used를 true로 변경한다.
        * */
        NicknameDTO randomNickname = nicknamesRepository.findRandomNickname();

        nicknamesRepository.updateIsUsed(randomNickname.getId(), true);

//        return nicknamesRepository.findRandomNickname();
        return randomNickname;
    }

    @Override
    @Transactional
    public MemberDTO postChangeNickname(CustomPrincipal principal) {

        /* 설명. 닉네임 repo에서 닉네임 가져오기
         *  1. is_used가 false인 닉네임을 가져온다.
         *  2. 가져온 닉네임을 사용자의 닉네임으로 설정한다.
         *  3. 사용한 닉네임은 is_used를 true로 변경한다.
         *  4. 기존 닉네임은 is_used를 false로 변경한다.
        * */

        NicknameDTO nicknameDTO = getRandomNickname();
        MemberDTO memberDTO = null;

        if (nicknameDTO != null) {
            Long memberId = principal.getId();

            try {
                // 닉네임을 사용자의 닉네임으로 설정
                memberRepository.updateNickname(memberId, nicknameDTO.getNickname());

                // 사용한 닉네임은 is_used를 true로 변경
                nicknamesRepository.updateIsUsed(nicknameDTO.getId(), true);

                // 기존 닉네임은 is_used를 false로 변경
                nicknamesRepository.updateIsUsedByNickname(principal.getNickname());

                memberDTO = new MemberDTO(
                        memberId,
                        nicknameDTO.getNickname(),
                        principal.getEmail(),
                        null
                );

            } catch (Exception e) {
                // 예외 처리 로직 추가 (예: 로그 기록, 사용자에게 에러 메시지 전달 등)
                log.error("Error updating nickname for member ID: {}", memberId, e);
                throw new GlobalCommonException(GlobalErrorCode.NICKNAME_UPDATE_FAILED);
            }
        }
        return memberDTO;
    }

}
