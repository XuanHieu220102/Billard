package com.billiard.app.fooditem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FoodItemResponse(
        UUID id,
        String name,
        BigDecimal price,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
