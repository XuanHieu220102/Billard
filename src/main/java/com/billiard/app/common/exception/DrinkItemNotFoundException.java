package com.billiard.app.common.exception;

import java.util.UUID;

public class DrinkItemNotFoundException extends ResourceNotFoundException {

    public DrinkItemNotFoundException(UUID drinkItemId) {
        super(ErrorCode.DRINK_ITEM_NOT_FOUND, "Drink item not found: " + drinkItemId);
    }
}
