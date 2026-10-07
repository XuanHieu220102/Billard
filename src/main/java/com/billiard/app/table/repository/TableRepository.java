package com.billiard.app.table.repository;

import com.billiard.app.table.entity.BilliardTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TableRepository extends JpaRepository<BilliardTable, UUID> {

    List<BilliardTable> findAllByShopIdOrderByTableNumberAsc(UUID shopId);

    Optional<BilliardTable> findByIdAndShopId(UUID id, UUID shopId);

    boolean existsByShopIdAndTableNumber(UUID shopId, String tableNumber);

    boolean existsByShopIdAndTableNumberAndIdNot(UUID shopId, String tableNumber, UUID id);
}
