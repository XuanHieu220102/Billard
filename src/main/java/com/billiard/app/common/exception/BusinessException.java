package com.billiard.app.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Base class for all business-rule exceptions. Carries an HTTP status and a
 * stable error code so the global exception handler can translate it into
 * the unified error response envelope without needing per-exception branches.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }
}
