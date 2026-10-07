package com.billiard.app.fooditem.repository;

import com.billiard.app.fooditem.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {

    List<FoodItem> findAllByShopIdOrderByNameAsc(UUID shopId);

    Optional<FoodItem> findByIdAndShopId(UUID id, UUID shopId);
}
