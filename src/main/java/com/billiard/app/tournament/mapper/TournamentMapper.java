package com.billiard.app.tournament.mapper;

import com.billiard.app.tournament.dto.TournamentMatchResponse;
import com.billiard.app.tournament.dto.TournamentParticipantResponse;
import com.billiard.app.tournament.dto.TournamentResponse;
import com.billiard.app.tournament.entity.Tournament;
import com.billiard.app.tournament.entity.TournamentMatch;
import com.billiard.app.tournament.entity.TournamentParticipant;
import org.springframework.stereotype.Component;

@Component
public class TournamentMapper {

    public TournamentResponse toResponse(Tournament tournament, int participantCount) {
        return new TournamentResponse(
                tournament.getId(),
                tournament.getName(),
                tournament.getFormat(),
                tournament.getEventDate(),
                tournament.getPrize(),
                tournament.getNote(),
                tournament.getStatus(),
                participantCount,
                tournament.getCreatedAt(),
                tournament.getUpdatedAt()
        );
    }

    public TournamentParticipantResponse toResponse(TournamentParticipant participant) {
        return new TournamentParticipantResponse(
                participant.getId(),
                participant.getDisplayName(),
                participant.getDisplayOrder()
        );
    }

    public TournamentMatchResponse toResponse(TournamentMatch match) {
        return new TournamentMatchResponse(
                match.getId(),
                match.getBracket(),
                match.getRound(),
                match.getMatchIndex(),
                match.getParticipant1Id(),
                match.getParticipant2Id(),
                match.getWinnerId(),
                match.getStatus(),
                match.getNextMatchId(),
                match.getNextMatchSlot(),
                match.getLoserNextMatchId(),
                match.getLoserNextMatchSlot()
        );
    }
}
