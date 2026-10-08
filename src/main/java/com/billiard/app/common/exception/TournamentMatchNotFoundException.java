package com.billiard.app.common.exception;

import java.util.UUID;

public class TournamentMatchNotFoundException extends ResourceNotFoundException {

    public TournamentMatchNotFoundException(UUID matchId) {
        super(ErrorCode.TOURNAMENT_MATCH_NOT_FOUND, "Tournament match not found: " + matchId);
    }
}
