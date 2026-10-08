package com.billiard.app.tournament.dto;

import com.billiard.app.tournament.entity.TournamentFormat;
import com.billiard.app.tournament.entity.TournamentStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Summary shape used for list views — no participants/matches payload. */
public record TournamentResponse(
        UUID id,
        String name,
        TournamentFormat format,
        LocalDate eventDate,
        String prize,
        String note,
        TournamentStatus status,
        int participantCount,
        Instant createdAt,
        Instant updatedAt
) {
}
