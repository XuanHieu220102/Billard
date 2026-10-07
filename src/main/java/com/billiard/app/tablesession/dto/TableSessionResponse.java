package com.billiard.app.tablesession.dto;

import com.billiard.app.tablesession.entity.TableSessionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TableSessionResponse(
        UUID id,
        UUID tableId,
        Instant startTime,
        Instant endTime,
        BigDecimal pricePerHourSnapshot,
        TableSessionStatus status,
        List<com.billiard.app.sessionorder.dto.SessionOrderResponse> orders,
        Instant createdAt,
        Instant updatedAt
) {
}
