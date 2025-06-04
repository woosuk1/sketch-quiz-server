package com.itcen.whiteboardserver.auth.service;

import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.enums.AuthProvider;
import com.itcen.whiteboardserver.member.enums.MemberRole;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.member.service.MemberService;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        // CustomPrincipal을 사용하여 UserDetails를 반환
        return new CustomPrincipal(member.getId(), member.getEmail(), member.getPassword(),
                member.getNickname(), member.getMemberRole());
    }

    @Transactional
    public void registerUser(String email, String rawPassword) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        Member user = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(memberService.getRandomNickname().getNickname()) // nicknames 테이블에서 랜덤으로 닉네임 가져오기
                .memberRole(Collections.singleton(MemberRole.MEMBER)) // 기본 역할 설정
                .provider(AuthProvider.LOCAL) // enum 필드: LOCAL, GOOGLE, KAKAO
                .build();
        memberRepository.save(user);
    }

    @Transactional
    public Member processOAuth2User(String email, Map<String, Object> attributes) {
        Optional<Member> existingUser = memberRepository.findByEmail(email);
        if(existingUser.isPresent()){
            return existingUser.get();
        }

        // 신규 사용자 처리
        Member newUser = Member.builder()
                .email(email)
                .password(passwordEncoder.encode("defaultPassword")) // OAuth2 사용자에게는 기본 비밀번호 설정
                .nickname(memberService.getRandomNickname().getNickname()) // nicknames 테이블에서 랜덤으로 들고오기
                .memberRole(Collections.singleton(MemberRole.MEMBER)) // 기본 역할 설정
                .provider(AuthProvider.GOOGLE) // enum 필드: LOCAL, GOOGLE, KAKAO
                .providerId((String) attributes.get("sub")) // google 고유 id
                .build();

        return memberRepository.save(newUser);
    }
}
