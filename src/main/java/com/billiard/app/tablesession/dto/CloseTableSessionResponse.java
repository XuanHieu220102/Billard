package com.billiard.app.tablesession.dto;

import com.billiard.app.invoice.dto.InvoiceResponse;

public record CloseTableSessionResponse(
        TableSessionResponse session,
        InvoiceResponse invoice
) {
}
