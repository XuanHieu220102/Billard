package com.billiard.app.table.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateTableRequest(

        @NotBlank(message = "Table number is required")
        String tableNumber,

        String tableType,

        @NotNull(message = "Price per hour is required")
        @Positive(message = "Price per hour must be positive")
        BigDecimal pricePerHour
) {
}
