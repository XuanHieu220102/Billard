package com.billiard.app.common.exception;

import java.util.UUID;

public class TableSessionNotFoundException extends ResourceNotFoundException {

    public TableSessionNotFoundException(UUID sessionId) {
        super(ErrorCode.TABLE_SESSION_NOT_FOUND, "Table session not found: " + sessionId);
    }
}
