package com.billiard.app.tournament.dto;

import com.billiard.app.tournament.entity.TournamentFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

/** Used for both create and update (DRAFT-only) of a tournament, including its participant list. */
public record SaveTournamentRequest(

        @NotBlank(message = "Tournament name is required")
        String name,

        @NotNull(message = "Format is required")
        TournamentFormat format,

        LocalDate eventDate,

        String prize,

        String note,

        /** Participant display names, in entry order. Blank/duplicate-trimmed entries are rejected. */
        List<String> participantNames
) {
}
