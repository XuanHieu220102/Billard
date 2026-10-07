package com.billiard.app.common.exception;

public class InvoiceAlreadyPaidException extends ConflictException {

    public InvoiceAlreadyPaidException(String message) {
        super(ErrorCode.INVOICE_ALREADY_PAID, message);
    }
}
