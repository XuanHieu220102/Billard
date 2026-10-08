package com.billiard.app.common.exception;

public class TournamentNotEditableException extends ConflictException {

    public TournamentNotEditableException(String message) {
        super(ErrorCode.TOURNAMENT_NOT_EDITABLE, message);
    }
}
