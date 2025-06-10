package com.itcen.whiteboardserver.member.enums;

import java.util.List;
import java.util.Random;

public enum ProfileColor {
    WHITE, BLUE, BABYPINK, ORANGE, YELLOW, HIDDEN, HOTPINK, PURPLE, LIME;

    private static final List<ProfileColor> VALUES = List.of(values());
    private static final Random RANDOM = new Random();

    public static ProfileColor getRandomColor() {
        int randomIndex = RANDOM.nextInt(100); // 0~99까지의 난수 생성

        if (randomIndex < 3) { // 3% 확률로 HIDDEN 선택
            return HIDDEN;
        } else {
            return VALUES.get(RANDOM.nextInt(VALUES.size() - 1)); // 나머지 색상 중 랜덤 선택
        }

    }
}
