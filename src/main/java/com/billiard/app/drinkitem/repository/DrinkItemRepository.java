package com.billiard.app.drinkitem.repository;

import com.billiard.app.drinkitem.entity.DrinkItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DrinkItemRepository extends JpaRepository<DrinkItem, UUID> {

    List<DrinkItem> findAllByShopIdOrderByNameAsc(UUID shopId);

    Optional<DrinkItem> findByIdAndShopId(UUID id, UUID shopId);

    /**
     * Pessimistic write lock to serialize concurrent stock deductions
     * (adding a drink to a session) and avoid overselling beyond stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DrinkItem d where d.id = :id and d.shopId = :shopId")
    Optional<DrinkItem> findByIdAndShopIdForUpdate(UUID id, UUID shopId);
}
