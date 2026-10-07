package com.billiard.app.common.exception;

import java.util.UUID;

public class SessionOrderNotFoundException extends ResourceNotFoundException {

    public SessionOrderNotFoundException(UUID orderId) {
        super(ErrorCode.SESSION_ORDER_NOT_FOUND, "Session order not found: " + orderId);
    }
}
