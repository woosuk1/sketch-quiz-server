package com.itcen.whiteboardserver.config.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 1) OAuth2 로그인 시작·콜백 경로는 noop 처리 (Spring Security 필터가 처리하도록)
        registry.addViewController("/oauth2/authorization/{registrationId}")
                .setViewName("noop");
        registry.addViewController("/login/oauth2/code/{registrationId}")
                .setViewName("noop");

        // 2) API 전용 테스트 페이지
        registry.addViewController("/api/auth/login")
                .setViewName("forward:/oauth2.html");
        // 루트도 SPA 테스트 페이지로
        registry.addViewController("/")
                .setViewName("forward:/oauth2.html");
        // 실패 콜백 쿼리 무시하고 같은 페이지로
        registry.addViewController("/api/auth/login?error")
                .setViewName("forward:/oauth2.html");
    }
}
