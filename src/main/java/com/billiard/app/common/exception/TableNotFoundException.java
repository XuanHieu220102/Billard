package com.billiard.app.common.exception;

import java.util.UUID;

public class TableNotFoundException extends ResourceNotFoundException {

    public TableNotFoundException(UUID tableId) {
        super(ErrorCode.TABLE_NOT_FOUND, "Table not found: " + tableId);
    }
}
