package com.billiard.app.invoice.dto;

import jakarta.validation.constraints.NotNull;

public record ApplyDiscountRequest(

        @NotNull(message = "Discount percent is required")
        Integer discountPercent
) {
}
