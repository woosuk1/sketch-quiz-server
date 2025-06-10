package com.itcen.whiteboardserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode {

    /**
     * 400(Bad Request)
     * 이 응답은 잘못된 문법으로 인하여 서버가 요청을 이해할 수 없음을 의미합니다.
     */
    WRONG_ENTRY_POINT(40000, HttpStatus.BAD_REQUEST, "잘못된 접근입니다."),
    MISSING_REQUEST_PARAMETER(40001, HttpStatus.BAD_REQUEST, "필수 요청 파라미터가 누락되었습니다."),
    INVALID_PARAMETER_FORMAT(40002, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자 형식입니다."),
    BAD_REQUEST_JSON(40003, HttpStatus.BAD_REQUEST, "잘못된 JSON 형식입니다."),
    // 데이터 무결성 위반 추가(ex: db의 NOT NULL 속성)
    DATA_INTEGRITY_VIOLATION(40005, HttpStatus.BAD_REQUEST,
            "데이터 무결성 위반입니다. 필수 값이 누락되었거나 유효하지 않습니다."),
    VALIDATION_FAIL(40007, HttpStatus.BAD_REQUEST, "유효성 검사 실패"),


    /**
     * 401(Unauthorized)
     * 비록 HTTP 표준에서는 "미승인(unauthorized)"를 명확히 하고 있지만,
     * 의미상 이 응답은 "비인증(unauthenticated)"을 의미합니다.
     * 클라이언트는 요청한 응답을 받기 위해서는 반드시 스스로를 인증해야 합니다.
     */
    INVALID_ACCESS_TOKEN(40100, HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    ACCESS_TOKEN_EXPIRED(40101, HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    INVALID_REFRESH_TOKEN(40102, HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_EXPIRED(40103, HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    OAUTH_UNAUTHORIZED(40104, HttpStatus.UNAUTHORIZED, "OAuth 인증 실패입니다."),

    /**
     * 403(Forbidden)
     * 클라이언트는 콘텐츠에 접근할 권리를 가지고 있지 않습니다.
     * 예를들어 그들은 미승인이어서 서버는 거절을 위한 적절한 응답을 보냅니다. 401과 다른 점은 서버가 클라이언트가 누구인지 알고 있습니다.
     */
    UNAUTHORIZED(40300, HttpStatus.FORBIDDEN, "접근 권한이 존재하지 않습니다."),


    /**
     * 404(Not Found)
     * 서버는 요청받은 리소스를 찾을 수 없습니다. 브라우저에서는 알려지지 않은 URL을 의미합니다.
     * 이것은 API에서 종점은 적절하지만 리소스 자체는 존재하지 않음을 의미할 수도 있습니다.
     * 서버들은 인증받지 않은 클라이언트로부터 리소스를 숨기기 위하여 이 응답을 403 대신에 전송할 수도 있습니다.
     * 이 응답 코드는 웹에서 반복적으로 발생하기 때문에 가장 유명할지도 모릅니다.
     */
    ENDPOINT_NOT_FOUND(40400, HttpStatus.NOT_FOUND, "존재하지 않는 엔드포인트입니다."),
    MEMBER_NOT_FOUND(40401, HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    NICKNAME_NOT_FOUND(40402, HttpStatus.NOT_FOUND, "존재하지 않는 닉네임입니다."),

    /**
     * 500(Internal Server Error)
     * 서버가 처리 방법을 모르는 상황이 발생했습니다. 서버는 아직 처리 방법을 알 수 없습니다.
     */
    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    DATABASE_CONNECTION_FAILURE(50001, HttpStatus.INTERNAL_SERVER_ERROR, "데이터 베이스 연결 실패입니다."),
    REDIS_ERROR(50002, HttpStatus.INTERNAL_SERVER_ERROR, "레디스 저장 실패입니다."),
    NICKNAME_UPDATE_FAILED(50003, HttpStatus.INTERNAL_SERVER_ERROR, "닉네임 업데이트 실패입니다."),;


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String msg;
}
