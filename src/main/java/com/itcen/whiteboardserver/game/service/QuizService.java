package com.itcen.whiteboardserver.game.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {
    private final List<String> quizWords = List.of(
            "아이티센", "아이티센 엔텍", "아이티센 클로잇",
            "아이티센 CTS", "아이티센 타워", "아이티센 글로벌",
            "아이티센 PNS", "아이티센 코어", "세니",
            "2005년", "15층", "강진모 회장님",
            "패밀리데이", "창의열정봉사", "카페 드 센",
            "inspire with technology", "헬스장", "동호회"
    );

    public List<String> getQuizWordsForGame(int totalTurn) {
        List<String> copyQuizWords = quizWords.stream().collect(Collectors.toList());
        Collections.shuffle(copyQuizWords);

        return copyQuizWords.subList(0, totalTurn);
    }

}
