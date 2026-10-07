package com.billiard.app.invoice.dto;

import com.billiard.app.invoice.entity.InvoiceStatus;
import com.billiard.app.sessionorder.dto.SessionOrderResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID tableSessionId,
        BigDecimal tableAmount,
        int discountPercent,
        BigDecimal tableAmountAfterDiscount,
        BigDecimal foodDrinkAmount,
        BigDecimal totalAmount,
        InvoiceStatus status,
        List<SessionOrderResponse> lineItems,
        Instant createdAt,
        Instant updatedAt
) {
}
