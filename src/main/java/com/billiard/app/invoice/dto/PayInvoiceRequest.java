package com.billiard.app.invoice.dto;

import com.billiard.app.invoice.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;

public record PayInvoiceRequest(

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod
) {
}
