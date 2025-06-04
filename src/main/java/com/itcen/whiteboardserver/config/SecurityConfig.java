package com.itcen.whiteboardserver.config;


import com.itcen.whiteboardserver.auth.service.CustomOAuth2UserService;
import com.itcen.whiteboardserver.auth.service.CustomOidcUserService;
import com.itcen.whiteboardserver.auth.service.UserDetailsServiceImpl;
import com.itcen.whiteboardserver.global.exception.GlobalCommonException;
import com.itcen.whiteboardserver.global.exception.GlobalErrorCode;
import com.itcen.whiteboardserver.security.filter.JwtAuthenticationFilter;
import com.itcen.whiteboardserver.security.filter.RedisRateLimitingFilter;
import com.itcen.whiteboardserver.security.filter.RequestResponseLoggingFilter;
import com.itcen.whiteboardserver.security.handler.CookieAuthorizationRequestRepository;
import com.itcen.whiteboardserver.security.handler.CustomAuthenticationEntryPoint;
import com.itcen.whiteboardserver.security.handler.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Spring Security 6.x configuration applying best practices:
 * 1. Access & Refresh tokens stored in HttpOnly cookies (SameSite=Lax)
 * 2. CSRF protection: Double-submit using Non-HttpOnly CSRF cookie + header check
 * 3. Redis TTL-based refresh-token storage
 * 4. Logout revokes Redis keys and clears cookies
 * 5. Rate limiting on refresh endpoint using distributed token bucket (Bucket4j + Redis)
 */
@Configuration
@EnableWebSecurity
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RedisRateLimitingFilter redisRateLimitingFilter;
    private final RequestResponseLoggingFilter requestResponseLoggingFilter;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomOidcUserService customOidcUserService;

//    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 가장 먼저 만날 필터(로깅 설정)
                .addFilterBefore(requestResponseLoggingFilter,
                        SecurityContextHolderFilter.class)
                // Rate limiting filter 를 먼저 chaining 하는 것은 비인증 사용자만 하면 된다
                .addFilterAfter(redisRateLimitingFilter, RequestResponseLoggingFilter.class)
                // CSRF config: HttpOnly cookie, SameSite=Lax, header X-XSRF-TOKEN

//                .csrf(csrf -> csrf
//                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
//
//                        // 로그인·회원가입만 제외
//                        .ignoringRequestMatchers(
//                                "/auth/login", "/auth/logout"
//                        )
//                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
//                // JWT auth filter -> 인증 필터보다 먼저 토큰 추출 및 검증을 하여 SecurityContext 설정
                .addFilterAfter(jwtAuthenticationFilter, SecurityContextHolderFilter.class)

                // OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/auth/login")
                        .authorizationEndpoint(a -> a
                                .baseUri("/oauth2/authorization")
                                // 쿠키 저장소: OAuth2 인가 요청(state) 보관
                                .authorizationRequestRepository(authorizationRequestRepository()
                            )
                        )
                        .redirectionEndpoint(r ->
                                r.baseUri("/login/oauth2/code/*"))
                        .userInfoEndpoint(u ->
                                u.userService(customOAuth2UserService)
                        // OIDC (openid) 용
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
//                        .failureUrl("/auth/login?error")

                        .failureHandler((request, response, exception) -> {
                            // 1) 예외 로그 찍기
                            log.error("OAuth2 로그인 실패: registrationId={}, uri={}",
                                    request.getParameter("registrationId"),
                                    request.getRequestURI(),
                                    exception
                            );
                            // 2) 사용자에게는 기존 failureUrl 과 동일하게 redirect
                            response.sendRedirect("/auth/login?error");
                        })
                )
                // Disable HTTP session
                .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Authorize endpoints
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/static/**", "/index.html", "/static/**", "/login.html","/images/**", "/oauth2.html","/favicon.ico", "/css/**", "/js/**").permitAll()
//                        .requestMatchers("/api/member/**").permitAll()
//                        .requestMatchers("/").permitAll()
//                        .requestMatchers("/login/oauth2/**", "/oauth2/**", "/api/auth/**").permitAll()
//                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//                        .anyRequest().authenticated()
                        .anyRequest().permitAll()
                )

                /* 설명. 보호된 api 접근 시*/
                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint(GlobalErrorCode.ACCESS_TOKEN_EXPIRED))
                        .accessDeniedHandler(new AccessDeniedHandlerImpl())
                );

        return http.build();
    }

    /*
     * CSRF token repository using latest builder for HttpOnly cookie.
     * Cookie name: XSRF-TOKEN, Header name: X-XSRF-TOKEN, SameSite=Lax
     */
    private CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieCustomizer(cookie -> cookie
                        .path("/")              // 모든 경로에 대해 CSRF 쿠키 적용
//                .secure(true)          // HTTPS 환경에서만 전송되도록
                        .sameSite("Lax")       // CSRF 방지 기본 전략
        );
        return repo;
    }

    /* 설명. 인코딩에 있어서 가용성이 좋은 팩토리*/
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * AuthenticationConfiguration 에서 컨텍스트에 등록된 AuthenticationManager를 꺼내 옵니다.
     * WebSecurityConfigurerAdapter 없이도 전역 AuthenticationManager 를 사용할 수 있게 해 줍니다.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            UserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder
    ) throws Exception {
        AuthenticationManagerBuilder authBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder);

        return authBuilder.build();
    }

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository(){
        return new CookieAuthorizationRequestRepository();
    }

}
