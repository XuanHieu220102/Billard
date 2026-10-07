package com.billiard.app.drinkitem.repository;

import com.billiard.app.drinkitem.entity.DrinkStockEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DrinkStockEntryRepository extends JpaRepository<DrinkStockEntry, UUID> {
}
