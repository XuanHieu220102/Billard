package com.billiard.app.tournament.repository;

import com.billiard.app.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TournamentRepository extends JpaRepository<Tournament, UUID> {

    Optional<Tournament> findByIdAndShopId(UUID id, UUID shopId);

    List<Tournament> findAllByShopIdOrderByCreatedAtDesc(UUID shopId);
}
