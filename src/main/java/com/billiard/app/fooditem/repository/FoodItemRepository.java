package com.billiard.app.fooditem.repository;

import com.billiard.app.fooditem.entity.FoodItem;
import com.billiard.app.fooditem.entity.FoodItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FoodItemRepository extends JpaRepository<FoodItem, UUID> {

    List<FoodItem> findAllByShopIdOrderByNameAsc(UUID shopId);

    List<FoodItem> findAllByShopIdAndCategoryOrderByNameAsc(UUID shopId, FoodItemCategory category);

    Optional<FoodItem> findByIdAndShopId(UUID id, UUID shopId);
}
