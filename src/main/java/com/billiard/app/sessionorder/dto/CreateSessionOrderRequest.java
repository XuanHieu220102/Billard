package com.billiard.app.sessionorder.dto;

import com.billiard.app.sessionorder.entity.ItemType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CreateSessionOrderRequest(

        @NotNull(message = "Item type is required")
        ItemType itemType,

        @NotNull(message = "Item id is required")
        UUID itemId,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        Integer quantity
) {
}
