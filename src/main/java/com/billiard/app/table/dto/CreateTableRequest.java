package com.billiard.app.table.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTableRequest(

        @NotBlank(message = "Table number is required")
        String tableNumber,

        String tableType,

        @Positive(message = "Price per hour must be positive")
        BigDecimal pricePerHour
) {
}
