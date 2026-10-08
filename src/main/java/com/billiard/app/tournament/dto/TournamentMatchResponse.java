package com.billiard.app.tournament.dto;

import com.billiard.app.tournament.entity.MatchBracket;
import com.billiard.app.tournament.entity.MatchStatus;

import java.util.UUID;

public record TournamentMatchResponse(
        UUID id,
        MatchBracket bracket,
        int round,
        int matchIndex,
        UUID participant1Id,
        UUID participant2Id,
        UUID winnerId,
        MatchStatus status,
        UUID nextMatchId,
        Integer nextMatchSlot,
        UUID loserNextMatchId,
        Integer loserNextMatchSlot
) {
}
