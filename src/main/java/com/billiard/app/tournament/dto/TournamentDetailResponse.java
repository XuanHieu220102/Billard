package com.billiard.app.tournament.dto;

import java.util.List;

/** Full shape used for the bracket view — tournament summary plus participants and matches. */
public record TournamentDetailResponse(
        TournamentResponse tournament,
        List<TournamentParticipantResponse> participants,
        List<TournamentMatchResponse> matches
) {
}
