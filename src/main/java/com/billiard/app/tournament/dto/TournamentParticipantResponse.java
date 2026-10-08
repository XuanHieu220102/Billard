package com.billiard.app.tournament.dto;

import java.util.UUID;

public record TournamentParticipantResponse(
        UUID id,
        String displayName,
        int displayOrder
) {
}
