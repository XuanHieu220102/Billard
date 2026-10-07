package com.billiard.app.common.exception;

public class TableAlreadyOccupiedException extends ConflictException {

    public TableAlreadyOccupiedException(String message) {
        super(ErrorCode.TABLE_ALREADY_OCCUPIED, message);
    }
}
