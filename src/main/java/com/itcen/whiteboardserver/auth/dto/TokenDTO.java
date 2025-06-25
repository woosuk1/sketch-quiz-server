package com.itcen.whiteboardserver.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO {
    private ResponseCookie refreshToken;
    private String accessToken;
}
