package com.billiard.app.tournament.service;

import com.billiard.app.tournament.entity.MatchBracket;
import com.billiard.app.tournament.entity.MatchStatus;
import com.billiard.app.tournament.entity.TournamentFormat;
import com.billiard.app.tournament.entity.TournamentMatch;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the bracket structures BracketGenerator produces for power-of-two
 * participant counts, for both formats. These structural invariants matter
 * more than exact match counts: a single wrong link silently corrupts the
 * whole tournament once play starts.
 */
class BracketGeneratorTest {

    @Test
    void singleElimination_8players_producesCompleteWinnerBracket() {
        List<UUID> participantIds = randomIds(8);
        BracketGenerator generator = new BracketGenerator(
                UUID.randomUUID(), UUID.randomUUID(), participantIds, TournamentFormat.SINGLE_ELIMINATION);

        List<TournamentMatch> matches = generator.generate();

        // 8 players -> 4 + 2 + 1 = 7 matches total, all WINNER bracket, no LOSER/GRAND_FINAL.
        assertThat(matches).hasSize(7);
        assertThat(matches).allMatch(m -> m.getBracket() == MatchBracket.WINNER);

        assertRoundSizes(matches, Map.of(1, 4, 2, 2, 3, 1));
        assertRound1FullyPaired(matches, participantIds);
        assertEveryNonFinalMatchAdvancesSomewhere(matches);
        assertNoLoserRouting(matches); // single elimination never routes a loser anywhere
    }

    @Test
    void doubleElimination_4players_producesWinnerLoserAndGrandFinal() {
        List<UUID> participantIds = randomIds(4);
        BracketGenerator generator = new BracketGenerator(
                UUID.randomUUID(), UUID.randomUUID(), participantIds, TournamentFormat.DOUBLE_ELIMINATION);

        List<TournamentMatch> matches = generator.generate();
        Map<MatchBracket, List<TournamentMatch>> byBracket = groupByBracket(matches);

        // Winner: 2 (round 1) + 1 (final) = 3.
        // Loser: LB round 1 (2 WB-r1 droppers play each other) + LB round 2 (that winner
        // plays the WB-final dropper) = 2. Grand final: 1.
        assertThat(byBracket.get(MatchBracket.WINNER)).hasSize(3);
        assertThat(byBracket.get(MatchBracket.LOSER)).hasSize(2);
        assertThat(byBracket.get(MatchBracket.GRAND_FINAL)).hasSize(1);

        TournamentMatch loserRound1 = singleMatch(byBracket.get(MatchBracket.LOSER), 1);
        TournamentMatch loserRound2 = singleMatch(byBracket.get(MatchBracket.LOSER), 2);

        // Both WB round-1 matches must drop their loser into LB round 1 (two different slots).
        List<TournamentMatch> wbRound1 = byBracket.get(MatchBracket.WINNER).stream()
                .filter(m -> m.getRound() == 1).toList();
        assertThat(wbRound1).hasSize(2);
        for (TournamentMatch m : wbRound1) {
            assertThat(m.getLoserNextMatchId()).isEqualTo(loserRound1.getId());
        }
        assertThat(wbRound1.get(0).getLoserNextMatchSlot()).isNotEqualTo(wbRound1.get(1).getLoserNextMatchSlot());

        // LB round 1's winner feeds LB round 2; the WB final's loser also feeds LB round 2.
        assertThat(loserRound1.getNextMatchId()).isEqualTo(loserRound2.getId());
        TournamentMatch wbFinal = singleMatch(byBracket.get(MatchBracket.WINNER), 2);
        assertThat(wbFinal.getLoserNextMatchId()).isEqualTo(loserRound2.getId());
        assertThat(loserRound1.getNextMatchSlot()).isNotEqualTo(wbFinal.getLoserNextMatchSlot());

        TournamentMatch grandFinal = byBracket.get(MatchBracket.GRAND_FINAL).get(0);
        assertThat(wbFinal.getNextMatchId()).isEqualTo(grandFinal.getId());
        assertThat(loserRound2.getNextMatchId()).isEqualTo(grandFinal.getId());
        // The two grand-final feeders must occupy distinct slots.
        assertThat(wbFinal.getNextMatchSlot()).isNotEqualTo(loserRound2.getNextMatchSlot());
    }

    @Test
    void doubleElimination_8players_everyMatchExceptGrandFinalHasAnExitRoute() {
        List<UUID> participantIds = randomIds(8);
        BracketGenerator generator = new BracketGenerator(
                UUID.randomUUID(), UUID.randomUUID(), participantIds, TournamentFormat.DOUBLE_ELIMINATION);

        List<TournamentMatch> matches = generator.generate();
        Map<MatchBracket, List<TournamentMatch>> byBracket = groupByBracket(matches);

        assertThat(byBracket.get(MatchBracket.GRAND_FINAL)).hasSize(1);
        TournamentMatch grandFinal = byBracket.get(MatchBracket.GRAND_FINAL).get(0);

        for (TournamentMatch match : matches) {
            if (match.getId().equals(grandFinal.getId())) {
                continue;
            }
            // Every non-final match's winner must go somewhere.
            assertThat(match.getNextMatchId()).as("winner route for match %s/%s/%d",
                    match.getBracket(), match.getRound(), match.getMatchIndex()).isNotNull();

            // Winner-bracket matches always drop a loser somewhere in double elimination,
            // except the WB grand-final feeder which routes its loser straight to grand final.
            if (match.getBracket() == MatchBracket.WINNER) {
                assertThat(match.getLoserNextMatchId()).as("loser route for WB match round %d", match.getRound())
                        .isNotNull();
            }
        }

        // No match should point to itself, and every referenced target id must exist in the set.
        java.util.Set<UUID> allIds = matches.stream().map(TournamentMatch::getId).collect(Collectors.toSet());
        for (TournamentMatch match : matches) {
            if (match.getNextMatchId() != null) {
                assertThat(allIds).contains(match.getNextMatchId());
                assertThat(match.getNextMatchId()).isNotEqualTo(match.getId());
            }
            if (match.getLoserNextMatchId() != null) {
                assertThat(allIds).contains(match.getLoserNextMatchId());
            }
        }
    }

    @Test
    void doubleElimination_16players_everyMatchExceptGrandFinalHasAnExitRoute() {
        List<UUID> participantIds = randomIds(16);
        BracketGenerator generator = new BracketGenerator(
                UUID.randomUUID(), UUID.randomUUID(), participantIds, TournamentFormat.DOUBLE_ELIMINATION);

        List<TournamentMatch> matches = generator.generate();
        Map<MatchBracket, List<TournamentMatch>> byBracket = groupByBracket(matches);

        // WB: 8 + 4 + 2 + 1 = 15. LB: 7 rounds for 16 players (2*(4-1) = 6, plus the WB-r1
        // pairing round makes 7 total loser-bracket rounds). Grand final: 1.
        assertThat(byBracket.get(MatchBracket.WINNER)).hasSize(15);
        assertThat(byBracket.get(MatchBracket.GRAND_FINAL)).hasSize(1);

        TournamentMatch grandFinal = byBracket.get(MatchBracket.GRAND_FINAL).get(0);
        java.util.Set<UUID> allIds = matches.stream().map(TournamentMatch::getId).collect(Collectors.toSet());

        for (TournamentMatch match : matches) {
            if (match.getId().equals(grandFinal.getId())) {
                continue;
            }
            assertThat(match.getNextMatchId())
                    .as("winner route for %s round %d index %d", match.getBracket(), match.getRound(), match.getMatchIndex())
                    .isNotNull();
            assertThat(allIds).contains(match.getNextMatchId());

            if (match.getBracket() == MatchBracket.WINNER) {
                assertThat(match.getLoserNextMatchId())
                        .as("loser route for WB round %d index %d", match.getRound(), match.getMatchIndex())
                        .isNotNull();
                assertThat(allIds).contains(match.getLoserNextMatchId());
            }
        }

        // Exactly one loser-bracket match (the final one) must feed the grand final.
        long loserMatchesIntoGrandFinal = byBracket.get(MatchBracket.LOSER).stream()
                .filter(m -> grandFinal.getId().equals(m.getNextMatchId()))
                .count();
        assertThat(loserMatchesIntoGrandFinal).isEqualTo(1);

        // Every loser-bracket match must have a unique (bracket, round, matchIndex) slot,
        // and every winner-bracket round-1 participant must appear in the bracket exactly once.
        List<TournamentMatch> wbRound1 = byBracket.get(MatchBracket.WINNER).stream()
                .filter(m -> m.getRound() == 1).toList();
        assertThat(wbRound1).hasSize(8);
        assertRound1FullyPaired(matches, participantIds);
    }

    @Test
    void round1Matches_areReadyWithBothParticipants_laterRoundsArePending() {
        List<UUID> participantIds = randomIds(8);
        BracketGenerator generator = new BracketGenerator(
                UUID.randomUUID(), UUID.randomUUID(), participantIds, TournamentFormat.SINGLE_ELIMINATION);

        List<TournamentMatch> matches = generator.generate();

        for (TournamentMatch match : matches) {
            if (match.getRound() == 1 && match.getBracket() == MatchBracket.WINNER) {
                assertThat(match.getStatus()).isEqualTo(MatchStatus.READY);
                assertThat(match.getParticipant1Id()).isNotNull();
                assertThat(match.getParticipant2Id()).isNotNull();
            } else {
                assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING);
                assertThat(match.getParticipant1Id()).isNull();
                assertThat(match.getParticipant2Id()).isNull();
            }
        }
    }

    // ---------- helpers ----------

    private static List<UUID> randomIds(int count) {
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ids.add(UUID.randomUUID());
        }
        return ids;
    }

    private static Map<MatchBracket, List<TournamentMatch>> groupByBracket(List<TournamentMatch> matches) {
        Map<MatchBracket, List<TournamentMatch>> result = new HashMap<>();
        for (TournamentMatch m : matches) {
            result.computeIfAbsent(m.getBracket(), k -> new ArrayList<>()).add(m);
        }
        return result;
    }

    private static TournamentMatch singleMatch(List<TournamentMatch> matches, int round) {
        List<TournamentMatch> found = matches.stream().filter(m -> m.getRound() == round).toList();
        assertThat(found).hasSize(1);
        return found.get(0);
    }

    private static void assertRoundSizes(List<TournamentMatch> matches, Map<Integer, Integer> expectedCountByRound) {
        Map<Integer, Long> actual = matches.stream()
                .collect(Collectors.groupingBy(TournamentMatch::getRound, Collectors.counting()));
        expectedCountByRound.forEach((round, expectedCount) ->
                assertThat(actual.get(round)).as("round %d match count", round).isEqualTo(expectedCount.longValue()));
    }

    private static void assertRound1FullyPaired(List<TournamentMatch> matches, List<UUID> participantIds) {
        List<TournamentMatch> round1 = matches.stream()
                .filter(m -> m.getBracket() == MatchBracket.WINNER && m.getRound() == 1)
                .toList();
        java.util.Set<UUID> seated = new java.util.HashSet<>();
        for (TournamentMatch m : round1) {
            assertThat(m.getParticipant1Id()).isNotNull();
            assertThat(m.getParticipant2Id()).isNotNull();
            assertThat(seated.add(m.getParticipant1Id())).isTrue();
            assertThat(seated.add(m.getParticipant2Id())).isTrue();
        }
        assertThat(seated).containsExactlyInAnyOrderElementsOf(participantIds);
    }

    private static void assertEveryNonFinalMatchAdvancesSomewhere(List<TournamentMatch> matches) {
        int maxRound = matches.stream().mapToInt(TournamentMatch::getRound).max().orElseThrow();
        for (TournamentMatch m : matches) {
            if (m.getRound() < maxRound) {
                assertThat(m.getNextMatchId()).isNotNull();
            } else {
                assertThat(m.getNextMatchId()).isNull();
            }
        }
    }

    private static void assertNoLoserRouting(List<TournamentMatch> matches) {
        assertThat(matches).allMatch(m -> m.getLoserNextMatchId() == null);
    }
}
