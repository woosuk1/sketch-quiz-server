package com.itcen.whiteboardserver.member.service;

import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberDTO getMemberById(Long id) {

        return memberRepository.findById(id)
                .map(member -> new MemberDTO(member.getId(), member.getName(), member.getEmail()))
                .orElse(null);
    }
}
