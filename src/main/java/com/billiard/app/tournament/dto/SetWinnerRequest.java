package com.billiard.app.tournament.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SetWinnerRequest(
        @NotNull(message = "Winner participant id is required")
        UUID winnerId
) {
}
