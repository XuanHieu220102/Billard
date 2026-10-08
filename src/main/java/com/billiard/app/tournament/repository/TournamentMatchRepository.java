package com.billiard.app.tournament.repository;

import com.billiard.app.tournament.entity.TournamentMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, UUID> {

    List<TournamentMatch> findAllByTournamentIdAndShopIdOrderByBracketAscRoundAscMatchIndexAsc(
            UUID tournamentId, UUID shopId);

    Optional<TournamentMatch> findByIdAndTournamentIdAndShopId(UUID id, UUID tournamentId, UUID shopId);
}
