package itcen.whiteboardserver.domain;

import lombok.Builder;

public record SocketConnInfo(
        String roomId,
        String userId
) {
}
