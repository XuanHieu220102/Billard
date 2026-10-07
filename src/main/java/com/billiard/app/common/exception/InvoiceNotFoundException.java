package com.billiard.app.common.exception;

import java.util.UUID;

public class InvoiceNotFoundException extends ResourceNotFoundException {

    public InvoiceNotFoundException(UUID invoiceId) {
        super(ErrorCode.INVOICE_NOT_FOUND, "Invoice not found: " + invoiceId);
    }
}
