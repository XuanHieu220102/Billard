package com.billiard.app.drinkitem.mapper;

import com.billiard.app.drinkitem.dto.DrinkItemResponse;
import com.billiard.app.drinkitem.entity.DrinkItem;
import org.springframework.stereotype.Component;

@Component
public class DrinkItemMapper {

    public DrinkItemResponse toResponse(DrinkItem item) {
        return new DrinkItemResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getStockQuantity(),
                item.isActive(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
