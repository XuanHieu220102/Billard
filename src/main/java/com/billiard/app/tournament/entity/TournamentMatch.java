package com.billiard.app.tournament.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * A single match slot in a tournament bracket. {@code nextMatchId}/{@code nextMatchSlot}
 * point to where the WINNER advances to. {@code loserNextMatchId}/{@code loserNextMatchSlot}
 * (double elimination only) point to where the LOSER drops down to in the Loser bracket -
 * null means the loser is eliminated outright (single elimination, or losing a second time).
 */
@Entity
@Table(name = "tournament_matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentMatch {

    @Id
    private UUID id;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "tournament_id", nullable = false)
    private UUID tournamentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchBracket bracket;

    @Column(nullable = false)
    private int round;

    @Column(name = "match_index", nullable = false)
    private int matchIndex;

    @Column(name = "participant1_id")
    private UUID participant1Id;

    @Column(name = "participant2_id")
    private UUID participant2Id;

    @Column(name = "winner_id")
    private UUID winnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @Column(name = "next_match_id")
    private UUID nextMatchId;

    @Column(name = "next_match_slot")
    private Integer nextMatchSlot;

    @Column(name = "loser_next_match_id")
    private UUID loserNextMatchId;

    @Column(name = "loser_next_match_slot")
    private Integer loserNextMatchSlot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = MatchStatus.PENDING;
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
