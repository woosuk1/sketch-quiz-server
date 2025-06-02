package com.itcen.whiteboardserver.game.exception;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ErrorResponse {
    private String type;
    private String message;
}