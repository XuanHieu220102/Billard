package com.billiard.app.common.exception;

public class DrinkOutOfStockException extends ConflictException {

    public DrinkOutOfStockException(String message) {
        super(ErrorCode.DRINK_OUT_OF_STOCK, message);
    }
}
