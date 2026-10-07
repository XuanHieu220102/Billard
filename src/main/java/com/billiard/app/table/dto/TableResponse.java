package com.billiard.app.table.dto;

import com.billiard.app.table.entity.TableStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TableResponse(
        UUID id,
        String tableNumber,
        String tableType,
        BigDecimal pricePerHour,
        TableStatus status,
        UUID activeSessionId,
        Instant activeSessionStartTime,
        Instant createdAt,
        Instant updatedAt
) {
}
