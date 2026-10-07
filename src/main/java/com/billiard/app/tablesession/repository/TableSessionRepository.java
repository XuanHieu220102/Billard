package com.billiard.app.tablesession.repository;

import com.billiard.app.tablesession.entity.TableSession;
import com.billiard.app.tablesession.entity.TableSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableSessionRepository extends JpaRepository<TableSession, UUID> {

    Optional<TableSession> findByIdAndShopId(UUID id, UUID shopId);

    Optional<TableSession> findByTableIdAndStatus(UUID tableId, TableSessionStatus status);

    List<TableSession> findAllByShopIdAndStatus(UUID shopId, TableSessionStatus status);

    List<TableSession> findAllByShopIdAndStatusOrderByStartTimeDesc(UUID shopId, TableSessionStatus status);

    List<TableSession> findAllByShopIdAndStatusAndStartTimeBetweenOrderByStartTimeDesc(
            UUID shopId, TableSessionStatus status, Instant from, Instant to);

    long countByShopIdAndStartTimeBetween(UUID shopId, Instant from, Instant to);
}
