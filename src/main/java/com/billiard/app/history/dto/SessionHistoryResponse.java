package com.billiard.app.history.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SessionHistoryResponse(
        UUID id,
        UUID tableId,
        String tableNumber,
        Instant startTime,
        Instant endTime,
        BigDecimal pricePerHourSnapshot
) {
}
