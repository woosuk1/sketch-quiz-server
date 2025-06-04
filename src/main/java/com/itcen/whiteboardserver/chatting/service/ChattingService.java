package com.itcen.whiteboardserver.chatting.service;

import com.itcen.whiteboardserver.chatting.dto.ChattingRequest;

public interface ChattingService {
    void chat(ChattingRequest chattingRequest);
}
