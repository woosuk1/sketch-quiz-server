package com.itcen.whiteboardserver.security.handler;

import com.itcen.whiteboardserver.auth.service.TokenService;
import com.itcen.whiteboardserver.global.exception.GlobalCommonException;
import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import com.itcen.whiteboardserver.member.dto.MemberDTO;
import com.itcen.whiteboardserver.member.enums.MemberRole;
import com.itcen.whiteboardserver.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final MemberService memberService;

    @Value("${FRONTEND_REDIRECT_URL}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {
//        OAuth2AuthenticationToken oauthToken =
//                (OAuth2AuthenticationToken) authentication;
//        String email = oauthToken.getPrincipal().getAttribute("email");
//
//        MemberDTO member = memberService.getMemberByEmail(email);
//
//        Set<MemberRole> rolesSet = member.getMemberRole();
//
//        List<String> roles = rolesSet.stream()
//                .map(MemberRole::name)
//                .collect(Collectors.toList());
//
//        // 3) 토큰 발급: 서비스에서 두 개의 ResponseCookie 반환
//        ResponseCookie[] cookies = tokenService.issueTokens(
//                email,
//                member.getNickname(),
//                String.valueOf(member.getId()),
//                roles,
//                member.getProfileColor().name()
//        );
//
//        // 4) 반환받은 쿠키들을 Response 헤더에 추가
//        //    (쿠키 객체 하나씩 toString()으로 헤더 값을 만들어 붙인다)
//        response.addHeader(HttpHeaders.SET_COOKIE, cookies[0].toString());
//        response.addHeader(HttpHeaders.SET_COOKIE, cookies[1].toString());
//
//        // 프론트엔드에 리디렉트
//        response.sendRedirect(frontendRedirectUrl);
    }
}
