# WebSocket 메시지 경로 가이드

## 목차

1. WebSocket 연결 설정
2. 메시지 송신 경로
3. 메시지 구독 경로
4. 에러 처리 구독 경로
5. 프론트엔드 구현 예시

## 1. WebSocket 연결 설정

### 연결 엔드포인트

``` 
/ws
```

### 연결 시 인증 방법

- 헤더를 통한 인증: `Authorization: Bearer {JWT 토큰}`
- URL 파라미터를 통한 인증: `?token={JWT 토큰}`

## 2. 메시지 송신 경로

프론트엔드에서 서버로 메시지를 보낼 때 사용하는 경로입니다.

| 기능      | 메시지 경로            | 필요한 데이터              |
|---------|-------------------|----------------------|
| 방 정보 조회 | `/app/room/info`  | `{ roomCode: Long }` |
| 방 참여    | `/app/room/join`  | `{ roomCode: Long }` |
| 방 나가기   | `/app/room/leave` | `{ roomId: Long }`   |

> 참고: 모든 요청에는 JWT 토큰으로부터 추출된 사용자 ID가 자동으로 포함됩니다.
>

## 3. 메시지 구독 경로

서버로부터 메시지를 받기 위해 구독해야 하는 경로입니다.

| 기능        | 구독 경로                  | 받는 데이터                            |
|-----------|------------------------|-----------------------------------|
| 방 정보 업데이트 | `/topic/room/{roomId}` | (방 정보와 참가자 목록) `RoomInfoResponse` |

## 4. 에러 처리 구독 경로

서버에서 발생하는 에러 메시지를 받기 위한 구독 경로입니다.

### 개인 에러 메시지 구독

``` 
/user/queue/errors
```

클라이언트 측에서는 위 경로로 구독하면 되며, 서버 내부적으로는 사용자 ID를 기반으로 메시지가 라우팅됩니다.

### 에러 응답 형식

``` json
{
  "type": "에러 유형 (예: '유효성 검사 오류', '실행 오류', '서버 오류')",
  "message": "상세 에러 메시지"
}
```

## 5. 프론트엔드 구현 예시

### WebSocket 연결 및 에러 처리 설정

``` javascript
// WebSocket 연결
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// 연결 헤더에 JWT 토큰 추가
const headers = {
  Authorization: 'Bearer ' + jwtToken
};

// 연결 및 구독 설정
stompClient.connect(headers, function(frame) {
  console.log('WebSocket 연결 성공');
  
  // 1. 개인 에러 메시지 구독
  stompClient.subscribe('/user/queue/errors', function(message) {
    const error = JSON.parse(message.body);
    console.error(`${error.type}: ${error.message}`);
    // 에러 알림 표시
    showErrorNotification(error.type, error.message);
  });
  
  // 2. 특정 방 정보 구독 (방에 참여할 때)
  function subscribeToRoom(roomId) {
    return stompClient.subscribe('/topic/room/' + roomId, function(message) {
      const roomInfo = JSON.parse(message.body);
      // 방 정보 및 참가자 목록 업데이트
      updateRoomInfo(roomInfo);
    });
  }
  
  // 3. 방 참여 요청 보내기
  function joinRoom(roomCode) {
    stompClient.send('/app/room/join', {}, JSON.stringify({
      roomCode: roomCode
    }));
  }
  
  // 4. 방 정보 요청하기
  function requestRoomInfo(roomCode) {
    stompClient.send('/app/room/info', {}, JSON.stringify({
      roomCode: roomCode
    }));
  }
  
  // 5. 방 나가기 요청 보내기
  function leaveRoom(roomId) {
    stompClient.send('/app/room/leave', {}, JSON.stringify({
      roomId: roomId
    }));
  }
}, function(error) {
  // 연결 실패 처리
  console.error('WebSocket 연결 실패:', error);
});

// 연결 해제
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  console.log('WebSocket 연결 해제');
}
```

## 주의사항

1. WebSocket 연결 요청에는 JWT 토큰을 통한 인증이 필요합니다.
2. 사용자별 에러 메시지는 `/user/queue/errors`로 구독해야 합니다.
3. 방 관련 이벤트(참가자 변경 등)는 `/topic/room/{roomId}`로 브로드캐스트됩니다.
4. `@Valid` 어노테이션이 적용된 필드의 유효성 검사 실패 시 `/user/queue/errors`로 에러 메시지가 전송됩니다.
5. 방에 참여하거나 방을 떠날 때 서비스 로직에서 발생한 예외도 `/user/queue/errors`로 전송됩니다.
