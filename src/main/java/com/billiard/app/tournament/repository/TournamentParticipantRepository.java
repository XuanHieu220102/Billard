package com.billiard.app.tournament.repository;

import com.billiard.app.tournament.entity.TournamentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TournamentParticipantRepository extends JpaRepository<TournamentParticipant, UUID> {

    List<TournamentParticipant> findAllByTournamentIdAndShopIdOrderByDisplayOrderAsc(UUID tournamentId, UUID shopId);

    void deleteAllByTournamentIdAndShopId(UUID tournamentId, UUID shopId);
}
