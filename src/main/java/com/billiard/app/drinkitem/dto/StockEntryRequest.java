package com.billiard.app.drinkitem.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockEntryRequest(

        @NotNull(message = "Quantity added is required")
        @Positive(message = "Quantity added must be a positive integer")
        Integer quantityAdded
) {
}
