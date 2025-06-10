package com.itcen.whiteboardserver.draw.dto;

import java.util.List;

public record DrawDto(
        Long turnId,
        String color,
        int width,
        List<Point> points
) {
    public record Point(int x, int y) {
    }
}


