package com.itcen.whiteboardserver.auth.controller;

import com.itcen.whiteboardserver.auth.dto.LoginRequest;
import com.itcen.whiteboardserver.auth.service.TokenService;
import com.itcen.whiteboardserver.auth.service.UserDetailsServiceImpl;
import com.itcen.whiteboardserver.security.principal.CustomPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authManager;
    private final TokenService tokenService;
//    private final PasswordEncoder passwordEncoder;
    private final UserDetailsServiceImpl usersService;

    /**
     * (0) 회원 가입
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @RequestBody @Valid LoginRequest req
    ) {
        usersService.registerUser(req.getUsername(), req.getPassword());
        return ResponseEntity.status(201).build();
    }

    /**
     * 1) 로그인 처리
     *  - AuthenticationManager 로 인증
     *  - SecurityContext에 Authentication 세팅
     *  - TokenService.issueTokens() 로 access/refresh 쿠키 설정
     *  - 클라이언트에는 200 OK만 반환 (토큰은 HttpOnly 쿠키로 자동 저장)
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest login,
                                      HttpServletResponse response) {
        try {
            // 1. 인증 시도(PasswordEncoder, UserDetailsService 자동 적용)
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            login.getUsername(), login.getPassword()
                    )
            );
//        SecurityContextHolder.getContext().setAuthentication(auth);

            CustomPrincipal principal = (CustomPrincipal) auth.getPrincipal();

            // 현재 인증된 유저의 역할 리스트 추출
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // 쿠키에 access/refresh 토큰 세팅
            tokenService.issueTokens(auth.getName(), principal.getNickname(), String.valueOf(principal.getId()), roles, response);

            // Body 없이 200 OK 리턴
            return ResponseEntity.ok().build();
        }catch(AuthenticationException ex){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 2) 토큰 리프레시 엔드포인트
     *  - TokenService.rotateRefresh() 가 쿠키를 갱신해 줌
     *  - 200 OK 리턴
     */
    @PostMapping("/oauth2/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest request,
                                        HttpServletResponse response) {
        tokenService.rotateRefresh(request, response);
        return ResponseEntity.ok().build();
    }

    /**
     * 3) 로그아웃
     *  - TokenService.logout() 으로 Redis 키 삭제 & 쿠키 만료
     *  - SecurityContext 비우고 204 No Content 리턴
     */
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       @AuthenticationPrincipal CustomPrincipal principal) {
        tokenService.logout(request, response, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public Map<String,Object> me(@AuthenticationPrincipal CustomPrincipal principal) {

        return Map.of(
                "email", principal.getEmail(),
                "nickname", principal.getNickname(),
                "id", principal.getId()
        );
    }
    @GetMapping("/protected")
    public String secret() {
        return "비밀 데이터";
    }
}