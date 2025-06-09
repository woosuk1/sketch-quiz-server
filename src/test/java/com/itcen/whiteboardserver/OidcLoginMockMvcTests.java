package com.itcen.whiteboardserver;

import com.itcen.whiteboardserver.auth.service.TokenService;
import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.entity.Member;
import com.itcen.whiteboardserver.member.enums.AuthProvider;
import com.itcen.whiteboardserver.member.enums.MemberRole;
import com.itcen.whiteboardserver.member.repository.MemberRepository;
import com.itcen.whiteboardserver.member.service.MemberService;
import com.itcen.whiteboardserver.member.service.MemberServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.security.oauth2.core.oidc.StandardClaimNames.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class OidcLoginMockMvcTests {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired private MemberServiceImpl memberServiceImpl;

    @MockitoBean TokenService tokenService;

    private Member saved;

    @Test
    @DisplayName("OIDC 로그인 성공 시,  custom principal 과 토큰 발급 흐름 검증")
    void oidcLogin_createsCustomPrincipal_andIssuesCookies() throws Exception {

        // --- Given ---
        // 테스트용 회원을 DB에 저장
        memberRepository.deleteAll();
        Member member = Member.builder()
                .email("test@kakao.com")
                .password(passwordEncoder.encode("irrelevant"))
                .nickname("코난")
                .memberRole(Collections.singleton(MemberRole.MEMBER))
                .provider(AuthProvider.KAKAO)
                .providerId("kakao-user-123")
                .build();
        saved = memberRepository.saveAndFlush(member);

        System.out.println("Saved Member: " + saved.getId());
        // 1) DB에서 조회한 MemberDTO
//        MemberDTO dbMember = MemberDTO.builder()
//                .id(12L)
//                .email("test@kakao.com")
//                .nickname("코난")
//                .memberRole(Set.of(MemberRole.MEMBER))
//                .build();
//        given(memberServiceImpl.getMemberByEmail("test@kakao.com")).willReturn(dbMember);
        MemberDTO dbMember = memberServiceImpl.getMemberByEmail("test@kakao.com");

        System.out.println("DB Member: " + dbMember.toString());

        // 2) TokenService 에서 발급할 쿠키
        ResponseCookie access  = ResponseCookie.from("access_token",  "AAA").build();
        ResponseCookie refresh = ResponseCookie.from("refresh_token", "RRR").build();
        given(tokenService.issueTokens(
//                eq("test@kakao.com"),
//                eq("코난"),
//                eq("12"),
//                eq(List.of("MEMBER"))
                eq(dbMember.getEmail()),
                eq(dbMember.getNickname()),
                eq(String.valueOf(dbMember.getId())),
                eq(List.of(MemberRole.MEMBER.name()))
        )).willReturn(new ResponseCookie[]{access, refresh});


        // 3) 임의 ClientRegistration (registrationId=kakao)
        ClientRegistration kakao = ClientRegistration.withRegistrationId("kakao")
                .clientId("dummyId")
                .clientSecret("dummySecret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "account-email")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .jwkSetUri("https://kauth.kakao.com/oauth/jwks")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("email")
                .clientName("Kakao")
                .build();

        // --- When & Then ---
        mockMvc.perform(get("/api/member")
                        .with(oidcLogin()
                                .clientRegistration(kakao)
                                // ID 토큰에 sub, email 넣기
                                .idToken(token -> token
                                        .claim("sub", "kakao-user-123")
                                        .claim("email", "test@kakao.com")
                                )
                                // UserInfo 에 nickname 넣기
                                .userInfoToken(ui -> ui.claim("nickname", "코난"))
                                // 권한은 ROLE_USER
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                        )
                )
                .andExpect(status().isOk())
                .andExpect(cookie().value("access_token",  "AAA"))
                .andExpect(cookie().value("refresh_token", "RRR"))
                // 컨트롤러가 리턴한 JSON 에 custom principal 정보가 포함되어야 함
                .andExpect(jsonPath("$.id").value(35))
                .andExpect(jsonPath("$.email").value("test@kakao.com"))
                .andExpect(jsonPath("$.nickname").value("코난"));
    }

    @Test
    @DisplayName("익명(비로그인) 시 /api/member 호출, 404 Not Found + MEMBER_NOT_FOUND 에러 코드 호출")
    void givenAnonymous_whenGetMember_thenNotFound() throws Exception {
        mockMvc.perform(get("/api/member"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(40401 ))
                .andExpect(jsonPath("$.message").value("존재하지 않는 회원입니다."));
    }
}