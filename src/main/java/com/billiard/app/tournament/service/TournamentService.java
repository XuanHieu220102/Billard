package com.billiard.app.tournament.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.TournamentMatchNotFoundException;
import com.billiard.app.common.exception.TournamentNotEditableException;
import com.billiard.app.common.exception.TournamentNotFoundException;
import com.billiard.app.common.exception.ValidationException;
import com.billiard.app.tournament.dto.SaveTournamentRequest;
import com.billiard.app.tournament.dto.SetWinnerRequest;
import com.billiard.app.tournament.dto.TournamentDetailResponse;
import com.billiard.app.tournament.dto.TournamentMatchResponse;
import com.billiard.app.tournament.dto.TournamentResponse;
import com.billiard.app.tournament.entity.MatchStatus;
import com.billiard.app.tournament.entity.Tournament;
import com.billiard.app.tournament.entity.TournamentMatch;
import com.billiard.app.tournament.entity.TournamentParticipant;
import com.billiard.app.tournament.entity.TournamentStatus;
import com.billiard.app.tournament.mapper.TournamentMapper;
import com.billiard.app.tournament.repository.TournamentMatchRepository;
import com.billiard.app.tournament.repository.TournamentParticipantRepository;
import com.billiard.app.tournament.repository.TournamentRepository;
import com.billiard.app.common.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private static final Logger log = LoggerFactory.getLogger(TournamentService.class);

    private final TournamentRepository tournamentRepository;
    private final TournamentParticipantRepository participantRepository;
    private final TournamentMatchRepository matchRepository;
    private final TournamentMapper mapper;
    private final CurrentShopProvider currentShopProvider;

    public TournamentService(TournamentRepository tournamentRepository,
                              TournamentParticipantRepository participantRepository,
                              TournamentMatchRepository matchRepository,
                              TournamentMapper mapper,
                              CurrentShopProvider currentShopProvider) {
        this.tournamentRepository = tournamentRepository;
        this.participantRepository = participantRepository;
        this.matchRepository = matchRepository;
        this.mapper = mapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public List<TournamentResponse> listTournaments() {
        UUID shopId = currentShopProvider.getCurrentShopId();
        return tournamentRepository.findAllByShopIdOrderByCreatedAtDesc(shopId).stream()
                .map(t -> mapper.toResponse(t, participantRepository
                        .findAllByTournamentIdAndShopIdOrderByDisplayOrderAsc(t.getId(), shopId).size()))
                .toList();
    }

    @Transactional
    public TournamentDetailResponse createTournament(SaveTournamentRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();

        Tournament tournament = Tournament.builder()
                .shopId(shopId)
                .name(request.name())
                .format(request.format())
                .eventDate(request.eventDate())
                .prize(request.prize())
                .note(request.note())
                .status(TournamentStatus.DRAFT)
                .build();
        tournament = tournamentRepository.save(tournament);

        saveParticipants(tournament.getId(), shopId, request.participantNames());

        log.info("Created tournament: tournamentId={}, shopId={}", tournament.getId(), shopId);
        return getDetail(tournament.getId());
    }

    @Transactional
    public TournamentDetailResponse updateTournament(UUID tournamentId, SaveTournamentRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Tournament tournament = getOwnedTournament(tournamentId, shopId);

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new TournamentNotEditableException(
                    "Only DRAFT tournaments can be edited: " + tournamentId);
        }

        tournament.setName(request.name());
        tournament.setFormat(request.format());
        tournament.setEventDate(request.eventDate());
        tournament.setPrize(request.prize());
        tournament.setNote(request.note());
        tournamentRepository.save(tournament);

        participantRepository.deleteAllByTournamentIdAndShopId(tournamentId, shopId);
        saveParticipants(tournamentId, shopId, request.participantNames());

        return getDetail(tournamentId);
    }

    private void saveParticipants(UUID tournamentId, UUID shopId, List<String> rawNames) {
        if (rawNames == null) {
            return;
        }
        List<String> names = rawNames.stream()
                .map(n -> n == null ? "" : n.trim())
                .filter(n -> !n.isEmpty())
                .toList();

        Instant now = Instant.now();
        List<TournamentParticipant> participants = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            participants.add(TournamentParticipant.builder()
                    .shopId(shopId)
                    .tournamentId(tournamentId)
                    .displayName(names.get(i))
                    .displayOrder(i)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        }
        participantRepository.saveAll(participants);
    }

    @Transactional
    public TournamentDetailResponse setupTournament(UUID tournamentId) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Tournament tournament = getOwnedTournament(tournamentId, shopId);

        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new TournamentNotEditableException(
                    "Only DRAFT tournaments can be set up: " + tournamentId);
        }

        List<TournamentParticipant> participants = participantRepository
                .findAllByTournamentIdAndShopIdOrderByDisplayOrderAsc(tournamentId, shopId);

        int count = participants.size();
        if (count < 4 || (count & (count - 1)) != 0) {
            throw new ValidationException(ErrorCode.TOURNAMENT_INVALID_PARTICIPANT_COUNT,
                    "Participant count must be a power of two (4, 8, 16, 32...), got: " + count);
        }

        List<UUID> participantIds = participants.stream().map(TournamentParticipant::getId).toList();
        BracketGenerator generator = new BracketGenerator(shopId, tournamentId, participantIds, tournament.getFormat());
        List<TournamentMatch> matches = generator.generate();
        matchRepository.saveAll(matches);

        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        tournamentRepository.save(tournament);

        log.info("Set up tournament bracket: tournamentId={}, shopId={}, participantCount={}, format={}",
                tournamentId, shopId, count, tournament.getFormat());
        return getDetail(tournamentId);
    }

    @Transactional(readOnly = true)
    public TournamentDetailResponse getDetail(UUID tournamentId) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Tournament tournament = getOwnedTournament(tournamentId, shopId);

        List<TournamentParticipant> participants = participantRepository
                .findAllByTournamentIdAndShopIdOrderByDisplayOrderAsc(tournamentId, shopId);
        List<TournamentMatch> matches = matchRepository
                .findAllByTournamentIdAndShopIdOrderByBracketAscRoundAscMatchIndexAsc(tournamentId, shopId);

        return new TournamentDetailResponse(
                mapper.toResponse(tournament, participants.size()),
                participants.stream().map(mapper::toResponse).toList(),
                matches.stream().map(mapper::toResponse).toList()
        );
    }

    @Transactional
    public TournamentMatchResponse setMatchWinner(UUID tournamentId, UUID matchId, SetWinnerRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Tournament tournament = getOwnedTournament(tournamentId, shopId);

        TournamentMatch match = matchRepository.findByIdAndTournamentIdAndShopId(matchId, tournamentId, shopId)
                .orElseThrow(() -> new TournamentMatchNotFoundException(matchId));

        if (match.getStatus() != MatchStatus.READY) {
            throw new ValidationException(ErrorCode.TOURNAMENT_MATCH_NOT_READY,
                    "Match is not ready for a result: " + matchId);
        }

        UUID winnerId = request.winnerId();
        if (!winnerId.equals(match.getParticipant1Id()) && !winnerId.equals(match.getParticipant2Id())) {
            throw new ValidationException(ErrorCode.TOURNAMENT_INVALID_WINNER,
                    "Winner must be one of the match's two participants: " + matchId);
        }
        UUID loserId = winnerId.equals(match.getParticipant1Id())
                ? match.getParticipant2Id()
                : match.getParticipant1Id();

        match.setWinnerId(winnerId);
        match.setStatus(MatchStatus.COMPLETED);
        matchRepository.save(match);

        Map<UUID, TournamentMatch> matchesById = matchRepository
                .findAllByTournamentIdAndShopIdOrderByBracketAscRoundAscMatchIndexAsc(tournamentId, shopId)
                .stream()
                .collect(Collectors.toMap(TournamentMatch::getId, m -> m));

        advancePlayer(matchesById, match.getNextMatchId(), match.getNextMatchSlot(), winnerId);

        // Loser drops to the loser bracket (double elimination) - null means elimination outright
        // (single elimination, or a second loss in the loser bracket).
        if (match.getLoserNextMatchId() != null) {
            advancePlayer(matchesById, match.getLoserNextMatchId(), match.getLoserNextMatchSlot(), loserId);
        }

        // The tournament's final match is whichever match has nowhere left to send its
        // winner: the GRAND_FINAL in double elimination, or the top WINNER-bracket round
        // in single elimination (which never gets a GRAND_FINAL match at all).
        if (match.getNextMatchId() == null) {
            tournament.setStatus(TournamentStatus.COMPLETED);
            tournamentRepository.save(tournament);
            log.info("Tournament completed: tournamentId={}, shopId={}, championId={}",
                    tournamentId, shopId, winnerId);
        }

        log.info("Recorded match winner: tournamentId={}, matchId={}, winnerId={}", tournamentId, matchId, winnerId);
        return mapper.toResponse(match);
    }

    private void advancePlayer(Map<UUID, TournamentMatch> matchesById, UUID targetMatchId, Integer slot, UUID playerId) {
        if (targetMatchId == null) {
            return;
        }
        TournamentMatch target = matchesById.get(targetMatchId);
        if (slot == 1) {
            target.setParticipant1Id(playerId);
        } else {
            target.setParticipant2Id(playerId);
        }
        if (target.getParticipant1Id() != null && target.getParticipant2Id() != null) {
            target.setStatus(MatchStatus.READY);
        }
        matchRepository.save(target);
    }

    private Tournament getOwnedTournament(UUID tournamentId, UUID shopId) {
        return tournamentRepository.findByIdAndShopId(tournamentId, shopId)
                .orElseThrow(() -> new TournamentNotFoundException(tournamentId));
    }
}
