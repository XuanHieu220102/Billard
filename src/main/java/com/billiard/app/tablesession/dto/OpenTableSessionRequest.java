package com.billiard.app.tablesession.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OpenTableSessionRequest(

        @NotNull(message = "Table id is required")
        UUID tableId
) {
}
