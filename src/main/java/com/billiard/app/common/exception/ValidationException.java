package com.billiard.app.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BusinessException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String code, String message) {
        super(HttpStatus.BAD_REQUEST, code, message);
    }
}
