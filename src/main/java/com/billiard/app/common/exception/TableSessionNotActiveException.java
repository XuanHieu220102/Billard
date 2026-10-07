package com.billiard.app.common.exception;

public class TableSessionNotActiveException extends ConflictException {

    public TableSessionNotActiveException(String message) {
        super(ErrorCode.TABLE_SESSION_NOT_ACTIVE, message);
    }
}
