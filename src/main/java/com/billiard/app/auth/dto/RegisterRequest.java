package com.billiard.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[0-9+][0-9]{6,14}$", message = "Phone number format is invalid")
        String phoneNumber,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        @NotBlank(message = "Password confirmation is required")
        String confirmPassword,

        @NotBlank(message = "Shop name is required")
        String shopName
) {
}
