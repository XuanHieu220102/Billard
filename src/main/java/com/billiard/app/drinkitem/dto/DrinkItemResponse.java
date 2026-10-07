package com.billiard.app.drinkitem.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record DrinkItemResponse(
        UUID id,
        String name,
        BigDecimal price,
        int stockQuantity,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
