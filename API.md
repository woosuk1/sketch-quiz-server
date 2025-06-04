# API 문서

이 문서는 서버의 REST API와 WebSocket 엔드포인트에 대한 정보를 제공합니다.

## 수정 이력

| 버전  | 날짜         | 작성자 | 변경 내용          |
|-----|------------|-----|----------------|
| 1.0 | 2025-06-02 | 임혁  | 최초 작성          |
| 1.1 | 2025-06-04 | 김유진 | 게임 진행 관련 내용 추가 | 

## 목차

- [REST API](#rest-api)
- [WebSocket](#websocket)
    - [연결 정보](#연결-정보)
    - [STOMP 엔드포인트](#stomp-엔드포인트)
    - [구독 토픽](#구독-토픽)
    - [에러 처리](#에러-처리)
- [이벤트 처리](#이벤트-처리)
- [데이터 구조](#데이터-구조)

## REST API

| 엔드포인트         | 메서드  | 설명                 | 필요 인증 | 응답             |
|---------------|------|--------------------|-------|----------------|
| `/api/member` | GET  | 현재 로그인한 사용자의 정보 조회 | O     | 사용자 이메일        |
| `/api/room`   | POST | 새로운 방 생성           | O     | `RoomResponse` |

## WebSocket

### 연결 정보

WebSocket 연결은 JWT 토큰 기반 인증을 사용합니다.

- 연결 엔드포인트: `/ws`
- SockJS 지원: O
- 인증 방식: 쿠키의 `access_token` 사용

### STOMP 엔드포인트 (Client -> Server )

| 엔드포인트                     | 설명       | 요청 본문              | 응답                                           |
|---------------------------|----------|--------------------|----------------------------------------------|
| `/app/room/join`          | 방 참여하기   | `RoomJoinRequest`  | 없음 (`/topic/room/{roomId}` 토픽으로 브로드캐스트)      |
| `/app/room/leave`         | 방 나가기    | 없음                 | 없음 (`/topic/room/{roomId}` 토픽으로 브로드캐스트)      |
| `/app/game/start`         | 게임 시작 요청 | `GameStartRequest` | 없음 (`/topic/room/{roomId}` 토픽으로 브로드캐스트)      |
| `/app/game/{gameId}/chat` | 채팅 전송    | `String`           | 없음 (`/topic/game/{gameId}/chat` 토픽으로 브로드캐스트) |
| `/app/game/{gameId}/draw` | 그리기 전송   | `DrawDto`          | 없음 (`/topic/game/{gameId}/draw` 토픽으로 브로드캐스트) |

### 구독 토픽 (Server -> Client)

| 토픽                          | 설명                               | 메시지 타입                     |
|-----------------------------|----------------------------------|----------------------------|
| `/topic/room/{roomId}`      | 특정 방의 상태 변경 알림 (참가자 변경, 호스트 변경 등) | `RoomInfoResponse`         |
| `/user/queue/errors`        | 사용자별 오류 메시지                      | `ErrorResponse`            |
| `/topic/game/{gameId}`      | 현재 게임 진행 관련 데이터 반환               | `TurnResponse<T>`          |
| `/user/topic/game/{gameId}` | 제출자에게만 제출 관련 데이터 반환              | `TurnResponse<DrawerData>` |
| `/topic/game/{gameId}/chat` | 현재 게임의 채팅 데이터 반환                 | `TurnResponse<String>`     | 
| `/topic/game/{gameId}/draw` | 현재 게임의 그리기 데이터 반환                | `TurnResponse<DrawDto>`    |


### 게임 관련 데이터 반환 정리 `TurnResponse<T>`

현재 게임을 진행하면서 생성되는 데이터는 TurnResponse<T> 형으로 반환됩니다.

```
public record TurnResponse<T>(TurnResponseType type, T data) {
}
```

이는 Type과 Data로 이루어져 있습니다. Type에 따라 분기 처리를 해주시면 될 것 같습니다.

| type  | 반환 데이터       | 구조                                                                                                                                                                                    | 설명                                                   |
|-------|--------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| TURN  | TurnData     | <pre>{<br>"turnId": 1,<br>"drawerId": 42,<br>"startTime": "2025-06-04T14:30:00",<br>"endTime": "2025-06-04T14:32:30"<br>}</pre>                                                       | 현재 턴이 시작될 때 턴 정보 브로드캐스팅                              |
| DRAWER | DrawerData   | <pre>{<br>  "quizWord": "apple",<br>  "turnId": 1<br>}</pre>                                                                                                                          | 턴이 시작될 때, 출제자에게 출제 정보 반환                             |
| CHAT | String       | "홍길동님이 정답을 맞추셨습니다."                                                                                                                                                                   | 참가자가 채팅을 치면 채팅을 브로드캐스팅                               | 
| FINISH | TurnQuitData | <pre>{<br>  "gameId": 1001,<br>  "members": [<br>    { "memberId": 1, "score": 150 },<br>    { "memberId": 2, "score": 120 },<br>    { "memberId": 3, "score": 90 }<br>  ]<br>}</pre> | 턴이 끝났을 때, 현재 회원들의 점수를 담은 TurnQuitData를 브로드캐스팅        | 
| CORRECT | CorrectData  |  <pre>{<br>  "memberId": 2,<br>  "turnId": 10,<br>  "gameId": 1001<br>}</pre>                                                                                                         | 참가자가 정답을 맞췄을 때, 맞춘 참가자 정보를 브로드캐스팅                    | 
| GAME_FINISH | TurnQuitData | <pre>{<br>  "gameId": 1001,<br>  "members": [<br>    { "memberId": 1, "score": 150 },<br>    { "memberId": 2, "score": 120 },<br>    { "memberId": 3, "score": 90 }<br>  ]<br>}</pre> | 게임이 끝났을 때, 모든 턴을 마친 사용자들의 점수를 담은 TurnQuitData를 브로드캐스팅 |
| DRAW | DrawDto | <pre>{<br>  "turnId": 10,<br>  "color": "#FF5733",<br>  "width": 5,<br>  "points": [<br>    { "x": 10, "y": 20 },<br>    { "x": 15, "y": 25 },<br>    { "x": 20, "y": 30 }<br>  ]<br>}</pre> | 현재 그리는 선분의 정보를 브로드캐스팅                                |

### 에러 처리

WebSocket 메시지 처리 중 발생하는 오류는 `/user/queue/errors` 엔드포인트를 통해 개별 사용자에게 전송됩니다.

| 예외 유형                             | 설명               | 응답 메시지 타입       |
|-----------------------------------|------------------|-----------------|
| `MethodArgumentNotValidException` | 유효성 검사 오류        | `ErrorResponse` |
| `RoomNotFoundException`           | 방을 찾을 수 없음       | `ErrorResponse` |
| `MemberNotFoundException`         | 회원을 찾을 수 없음      | `ErrorResponse` |
| `RoomJoinException`               | 방 참여 중 발생한 오류    | `ErrorResponse` |
| `RuntimeException`                | 실행 중 발생한 일반적인 오류 | `ErrorResponse` |
| 기타 모든 예외                          | 서버 내부 오류         | `ErrorResponse` |

## 이벤트 처리

서버는 다음과 같은 이벤트를 자동으로 처리합니다:

### WebSocket 연결 해제 처리

사용자가 WebSocket 연결을 끊으면 자동으로 방에서 나가는 처리가 진행됩니다.

### 방 상태 변경 알림

다음 상황에서 `/topic/room/{roomId}` 토픽으로 업데이트된 방 정보가 전송됩니다:

- 방 참가자 변경 시 (`RoomParticipantChangedEvent`)
- 방 호스트 변경 시 (`RoomHostChangedEvent`)

## 데이터 구조

### 요청 객체

#### RoomJoinRequest

- `roomCode`: 참여할 방 코드

#### GameStartRequest

- `roomCode`: 방 코드

### 응답 객체

#### RoomResponse

- `roomCode`: 방 코드

#### RoomInfoResponse

- `participantList`: 방 참가자 목록
    - `memberId`: 참가자 ID
    - `memberName`: 참가자 이름
    - `isHost`: 호스트 여부
- `roomCode`: 참여한 방 코드

#### ErrorResponse

- `type`: 오류 유형 ("유효성 검사 오류", "방 오류", "회원 오류", "방 참여 오류", "실행 오류", "서버 오류" 등)
- `message`: 상세 오류 메시지