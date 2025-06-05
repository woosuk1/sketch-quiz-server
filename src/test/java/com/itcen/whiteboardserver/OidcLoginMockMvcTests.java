//package com.itcen.whiteboardserver;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class OidcLoginMockMvcTests {
//
//    /* 설명.
//     *  MockMvc 주입으로 application context 생성 및 필터 체인 동작
//    * */
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Test
//    @DisplayName("OIDC 로그인 성공 후, 보호된 API에 접근하면 200 OK 반환")
//    void oidcLogin_and_accessProtectedApi() throws Exception {
//
//        /* 설명. Mock user claims 정의(sub, email) */
//        Map<String, Object> claims = Map.of(
//                StandardClaimNames.SUB, "sub",
//                StandardClaimNames.EMAIL, "test@example.com",
//                "nickname",               "테스트유저"
//        );
//
//        /* 설명. MockMvc 요청 시, with(oidcLogin())으로 인증 정보 모킹 */
//        mockMvc.perform(get("/api/member")
//                .with(oidcLogin()
//                        .idToken(id -> id
//                                .claim(StandardClaimNames.SUB, claims.get(StandardClaimNames.SUB))
//                                .claim(StandardClaimNames.EMAIL, claims.get(StandardClaimNames.EMAIL))
//                        )
//                        .userInfoToken(attrs -> {
//                            attrs.put("nickname", claims.get("nickname"));
//                        })
//                )
//            )
//                /* 설명.
//                 *  Oauth2LoginSuccessHandler가 CustomPrincipal로 교체하면
//                 *  controller가 받아서 정상 memberResponseDTO 반환
//                * */
//                .andExpect(status().isOk())
//                /* 설명. 응답값들이 return 되는지 검증 */
//                .andExpect(jsonPath("$.email").value("test@example.com"))
//                .andExpect(jsonPath("$.nickname").value("
//    }
//
//
//}
