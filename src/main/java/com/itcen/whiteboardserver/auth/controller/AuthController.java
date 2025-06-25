package com.itcen.whiteboardserver.auth.controller;

import com.itcen.whiteboardserver.auth.dto.TokenDTO;
import com.itcen.whiteboardserver.auth.service.TokenService;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final TokenService tokenService;

    /**
     * 2) 토큰 리프레시 엔드포인트
     *    - 클라이언트는 빈 바디로 "POST /api/auth/oauth2/refresh" 호출
     *    - @CookieValue 로 "refresh_token" 쿠키 값을 바로 받아온다.
     *    - TokenService가 검증 후 새 토큰 쿠키를 생성하여 반환.
     */
    @PostMapping("/oauth2/refresh")
//    public ResponseEntity<Void> refresh(
    public ResponseEntity<?> refresh(
            @Parameter(hidden = true) @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) {
//        ResponseCookie[] cookies =tokenService.rotateRefresh(refreshToken);
        TokenDTO token =tokenService.rotateRefresh(refreshToken);
//        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, cookies[0].toString())
//                .header(HttpHeaders.SET_COOKIE, cookies[1].toString())
//                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, token.getRefreshToken().toString())
                .body(Map.of(
                        "access_token", token.getAccessToken(),
                        "token_type", "Bearer",
                        "expires_in", 3600
                ));
    }

    /**
     * 3) 로그아웃
     *  - TokenService.logout() 으로 Redis 키 삭제 & 쿠키 만료
     *  - SecurityContext 비우고 204 No Content 리턴
     */
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CustomPrincipal principal) {
        ResponseCookie[] cookies = tokenService.logout(principal);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies[0].toString())
                .header(HttpHeaders.SET_COOKIE, cookies[1].toString())
                .build();
    }

    /**
     * 1) 로그인 엔드포인트
     *    - 클라이언트 oauth 로그인 성공 시 로그인 엔드포인트 호출
     *    - OAuthToken의 claim을 추출하여 사용자의 정보를 조회,
     *    - 액세스 토큰 body, 리프레시 토큰 쿠키를 생성하여 반환.
     *    - 프론트엔드에서는 전역 상태 변수에 관리 및 랜딩페이지로 redirect
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<?> login(
            Authentication authentication
    ) {
        TokenDTO token = tokenService.login(authentication);


        log.debug("chec2k---\n" + token.getAccessToken());
        log.debug(token.getRefreshToken().toString());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, token.getRefreshToken().toString())
                .body(Map.of(
                        "access_token", token.getAccessToken(),
                        "token_type", "Bearer",
                        "expires_in", 3600
                ));
    }
}