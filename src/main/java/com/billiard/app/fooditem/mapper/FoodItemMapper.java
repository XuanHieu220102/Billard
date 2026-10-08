package com.billiard.app.fooditem.mapper;

import com.billiard.app.fooditem.dto.FoodItemResponse;
import com.billiard.app.fooditem.entity.FoodItem;
import org.springframework.stereotype.Component;

@Component
public class FoodItemMapper {

    public FoodItemResponse toResponse(FoodItem item) {
        return new FoodItemResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getCategory(),
                item.isActive(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
