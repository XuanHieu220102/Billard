package com.billiard.app.fooditem.dto;

import com.billiard.app.fooditem.entity.FoodItemCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record FoodItemResponse(
        UUID id,
        String name,
        BigDecimal price,
        FoodItemCategory category,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt
) {
}
