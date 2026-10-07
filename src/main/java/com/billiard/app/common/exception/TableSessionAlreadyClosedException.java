package com.billiard.app.common.exception;

public class TableSessionAlreadyClosedException extends ConflictException {

    public TableSessionAlreadyClosedException(String message) {
        super(ErrorCode.TABLE_SESSION_ALREADY_CLOSED, message);
    }
}
