package com.billiard.app.shop.repository;

import com.billiard.app.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShopRepository extends JpaRepository<Shop, UUID> {
}
