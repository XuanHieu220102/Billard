package com.billiard.app.common.exception;

import java.util.UUID;

public class FoodItemNotFoundException extends ResourceNotFoundException {

    public FoodItemNotFoundException(UUID foodItemId) {
        super(ErrorCode.FOOD_ITEM_NOT_FOUND, "Food item not found: " + foodItemId);
    }
}
