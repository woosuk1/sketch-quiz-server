package com.itcen.whiteboardserver.game.exception;

public class RoomNotFoundException extends RuntimeException{
    public RoomNotFoundException(String message) {
        super(message);
    }
}
