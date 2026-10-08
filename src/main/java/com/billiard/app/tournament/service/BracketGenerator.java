package com.billiard.app.tournament.service;

import com.billiard.app.tournament.entity.MatchBracket;
import com.billiard.app.tournament.entity.TournamentFormat;
import com.billiard.app.tournament.entity.TournamentMatch;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Builds the full set of {@link TournamentMatch} rows for a freshly set-up tournament.
 *
 * <p>Single elimination is a plain binary bracket. Double elimination additionally builds
 * a Loser bracket that alternates between two kinds of rounds:
 * <ul>
 *   <li><b>Minor round</b>: winners of the previous Loser-bracket round play each other
 *       (no new droppers this round).</li>
 *   <li><b>Major round</b>: winners of the previous Loser-bracket round play the players
 *       who just dropped from the matching Winner-bracket round.</li>
 * </ul>
 * This is the conventional structure used by standard double-elimination brackets for a
 * participant count that is a power of two.
 */
public class BracketGenerator {

    private final UUID shopId;
    private final UUID tournamentId;
    private final List<UUID> participantIdsInBracketOrder;
    private final TournamentFormat format;
    private final List<TournamentMatch> matches = new ArrayList<>();

    public BracketGenerator(UUID shopId, UUID tournamentId, List<UUID> participantIds, TournamentFormat format) {
        this.shopId = shopId;
        this.tournamentId = tournamentId;
        this.participantIdsInBracketOrder = shuffle(participantIds);
        this.format = format;
    }

    private static List<UUID> shuffle(List<UUID> ids) {
        List<UUID> copy = new ArrayList<>(ids);
        Collections.shuffle(copy);
        return copy;
    }

    public List<TournamentMatch> generate() {
        buildWinnerBracket();

        if (format == TournamentFormat.DOUBLE_ELIMINATION) {
            buildLoserBracketAndGrandFinal();
        }

        return matches;
    }

    // ---------- Winner bracket ----------

    private void buildWinnerBracket() {
        int participantCount = participantIdsInBracketOrder.size();
        int totalRounds = Integer.numberOfTrailingZeros(participantCount); // log2(N)

        List<TournamentMatch> previousRound = null;

        for (int round = 1; round <= totalRounds; round++) {
            int matchesInRound = participantCount / (1 << round);
            List<TournamentMatch> currentRound = new ArrayList<>();

            for (int i = 0; i < matchesInRound; i++) {
                TournamentMatch match = newMatch(MatchBracket.WINNER, round, i);
                if (round == 1) {
                    match.setParticipant1Id(participantIdsInBracketOrder.get(i * 2));
                    match.setParticipant2Id(participantIdsInBracketOrder.get(i * 2 + 1));
                    match.setStatus(com.billiard.app.tournament.entity.MatchStatus.READY);
                }
                currentRound.add(match);
                matches.add(match);
            }

            if (previousRound != null) {
                linkRoundToNext(previousRound, currentRound);
            }
            previousRound = currentRound;
        }
    }

    /** Standard bracket pairing: winners of match 2i and 2i+1 feed into match i of the next round. */
    private void linkRoundToNext(List<TournamentMatch> round, List<TournamentMatch> nextRound) {
        for (int i = 0; i < round.size(); i++) {
            TournamentMatch match = round.get(i);
            TournamentMatch nextMatch = nextRound.get(i / 2);
            match.setNextMatchId(nextMatch.getId());
            match.setNextMatchSlot(i % 2 == 0 ? 1 : 2);
        }
    }

    // ---------- Loser bracket + grand final (double elimination only) ----------

    private void buildLoserBracketAndGrandFinal() {
        List<TournamentMatch> winnerMatches = new ArrayList<>(matches); // snapshot: winner bracket only so far
        int wbTotalRounds = Integer.numberOfTrailingZeros(participantIdsInBracketOrder.size());

        // Group winner-bracket matches by round for easy lookup of droppers per round.
        List<List<TournamentMatch>> winnerRounds = new ArrayList<>();
        for (int round = 1; round <= wbTotalRounds; round++) {
            final int r = round;
            winnerRounds.add(winnerMatches.stream().filter(m -> m.getRound() == r).toList());
        }

        List<TournamentMatch> previousLoserRound = null;
        int loserRoundNumber = 0;

        // LB round 1 is always a "major" round: pairs up WB round-1 droppers directly.
        List<TournamentMatch> wbRound1Droppers = winnerRounds.get(0);
        loserRoundNumber++;
        List<TournamentMatch> lbRound1 = new ArrayList<>();
        for (int i = 0; i < wbRound1Droppers.size() / 2; i++) {
            TournamentMatch match = newMatch(MatchBracket.LOSER, loserRoundNumber, i);
            lbRound1.add(match);
            matches.add(match);
        }
        linkLoserDrop(wbRound1Droppers, lbRound1, true);
        previousLoserRound = lbRound1;

        // From WB round 2 onward: first shrink the loser bracket down to match this WB
        // round's dropper count via "minor" rounds (LB winners play each other), then run
        // a "major" round where LB survivors absorb this WB round's droppers one-to-one.
        for (int wbRound = 2; wbRound <= wbTotalRounds; wbRound++) {
            List<TournamentMatch> droppers = winnerRounds.get(wbRound - 1);

            while (previousLoserRound.size() > droppers.size()) {
                loserRoundNumber++;
                List<TournamentMatch> minorRound = new ArrayList<>();
                for (int i = 0; i < previousLoserRound.size() / 2; i++) {
                    minorRound.add(newMatch(MatchBracket.LOSER, loserRoundNumber, i));
                }
                matches.addAll(minorRound);
                linkLoserAdvance(previousLoserRound, minorRound);
                previousLoserRound = minorRound;
            }

            // Major round: previous loser-round survivors (now equal in count to the
            // droppers) each absorb one dropper, one-to-one.
            loserRoundNumber++;
            List<TournamentMatch> majorRound = new ArrayList<>();
            for (int i = 0; i < droppers.size(); i++) {
                majorRound.add(newMatch(MatchBracket.LOSER, loserRoundNumber, i));
            }
            matches.addAll(majorRound);
            linkLoserAdvance(previousLoserRound, majorRound);
            linkLoserDrop(droppers, majorRound, false);
            previousLoserRound = majorRound;
        }

        // Grand final: Winner-bracket champion vs the sole Loser-bracket survivor.
        TournamentMatch winnerFinal = winnerMatches.get(winnerMatches.size() - 1);
        TournamentMatch loserFinal = previousLoserRound.get(0);
        TournamentMatch grandFinal = newMatch(MatchBracket.GRAND_FINAL, 1, 0);
        matches.add(grandFinal);

        winnerFinal.setNextMatchId(grandFinal.getId());
        winnerFinal.setNextMatchSlot(1);
        loserFinal.setNextMatchId(grandFinal.getId());
        loserFinal.setNextMatchSlot(2);
    }

    /**
     * Wires the LOSER of each match in {@code sourceRound} into {@code targetRound}.
     * When {@code pairAdjacent} is true, losers of sourceRound[2i] and sourceRound[2i+1]
     * feed the same targetRound[i] match (used only for the very first loser round, where
     * two same-round WB droppers are paired against each other). Otherwise each source
     * match's loser feeds one target match one-to-one (used for later "major" rounds,
     * where each dropper joins a loser-bracket survivor already waiting in that slot).
     */
    private void linkLoserDrop(List<TournamentMatch> sourceRound, List<TournamentMatch> targetRound,
                                boolean pairAdjacent) {
        if (pairAdjacent) {
            for (int i = 0; i < sourceRound.size(); i++) {
                TournamentMatch source = sourceRound.get(i);
                TournamentMatch target = targetRound.get(i / 2);
                source.setLoserNextMatchId(target.getId());
                source.setLoserNextMatchSlot(i % 2 == 0 ? 1 : 2);
            }
        } else {
            for (int i = 0; i < sourceRound.size(); i++) {
                TournamentMatch source = sourceRound.get(i);
                TournamentMatch target = targetRound.get(i);
                source.setLoserNextMatchId(target.getId());
                source.setLoserNextMatchSlot(2); // slot 1 reserved for the loser-bracket survivor
            }
        }
    }

    /** Wires the WINNER of each match in a loser-bracket round into the next loser-bracket round. */
    private void linkLoserAdvance(List<TournamentMatch> sourceRound, List<TournamentMatch> targetRound) {
        boolean collapsing = sourceRound.size() == targetRound.size(); // major round: 1-to-1, slot 1
        for (int i = 0; i < sourceRound.size(); i++) {
            TournamentMatch source = sourceRound.get(i);
            TournamentMatch target = collapsing ? targetRound.get(i) : targetRound.get(i / 2);
            source.setNextMatchId(target.getId());
            source.setNextMatchSlot(collapsing ? 1 : (i % 2 == 0 ? 1 : 2));
        }
    }

    private TournamentMatch newMatch(MatchBracket bracket, int round, int matchIndex) {
        Instant now = Instant.now();
        return TournamentMatch.builder()
                .id(UUID.randomUUID())
                .shopId(shopId)
                .tournamentId(tournamentId)
                .bracket(bracket)
                .round(round)
                .matchIndex(matchIndex)
                .status(com.billiard.app.tournament.entity.MatchStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
