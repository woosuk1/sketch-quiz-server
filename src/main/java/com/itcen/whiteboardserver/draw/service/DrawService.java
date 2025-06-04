package com.itcen.whiteboardserver.draw.service;

import com.itcen.whiteboardserver.draw.dto.DrawDto;

public interface DrawService {
    void draw(DrawDto drawDto, Long gameId, String email);
}
