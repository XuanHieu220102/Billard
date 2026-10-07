package com.billiard.app.sessionorder.dto;

import com.billiard.app.sessionorder.entity.ItemType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionOrderResponse(
        UUID id,
        UUID tableSessionId,
        ItemType itemType,
        UUID itemId,
        String itemName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        Instant createdAt,
        Instant updatedAt
) {
}
