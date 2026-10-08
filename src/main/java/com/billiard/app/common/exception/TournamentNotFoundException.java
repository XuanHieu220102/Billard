package com.billiard.app.common.exception;

import java.util.UUID;

public class TournamentNotFoundException extends ResourceNotFoundException {

    public TournamentNotFoundException(UUID tournamentId) {
        super(ErrorCode.TOURNAMENT_NOT_FOUND, "Tournament not found: " + tournamentId);
    }
}
