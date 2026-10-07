package com.billiard.app.common.exception;

public class TableNotAvailableException extends ConflictException {

    public TableNotAvailableException(String message) {
        super(ErrorCode.TABLE_NOT_AVAILABLE, message);
    }
}
