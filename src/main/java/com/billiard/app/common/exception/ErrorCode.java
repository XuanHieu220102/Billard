package com.billiard.app.common.exception;

/**
 * Central catalog of business error codes returned in the {@code code} field
 * of the error response envelope.
 */
public final class ErrorCode {

    private ErrorCode() {
    }

    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ACCESS_DENIED = "ACCESS_DENIED";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

    // Auth
    public static final String PHONE_ALREADY_REGISTERED = "PHONE_ALREADY_REGISTERED";
    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String PASSWORD_CONFIRMATION_MISMATCH = "PASSWORD_CONFIRMATION_MISMATCH";

    // Table
    public static final String TABLE_NOT_FOUND = "TABLE_NOT_FOUND";
    public static final String TABLE_NUMBER_ALREADY_EXISTS = "TABLE_NUMBER_ALREADY_EXISTS";
    public static final String TABLE_ALREADY_OCCUPIED = "TABLE_ALREADY_OCCUPIED";
    public static final String TABLE_NOT_AVAILABLE = "TABLE_NOT_AVAILABLE";
    public static final String TABLE_INVALID_STATE_TRANSITION = "TABLE_INVALID_STATE_TRANSITION";

    // Table session
    public static final String TABLE_SESSION_NOT_FOUND = "TABLE_SESSION_NOT_FOUND";
    public static final String TABLE_SESSION_ALREADY_CLOSED = "TABLE_SESSION_ALREADY_CLOSED";
    public static final String TABLE_SESSION_NOT_ACTIVE = "TABLE_SESSION_NOT_ACTIVE";

    // Food / Drink catalog
    public static final String FOOD_ITEM_NOT_FOUND = "FOOD_ITEM_NOT_FOUND";
    public static final String DRINK_ITEM_NOT_FOUND = "DRINK_ITEM_NOT_FOUND";
    public static final String DRINK_OUT_OF_STOCK = "DRINK_OUT_OF_STOCK";

    // Session order
    public static final String SESSION_ORDER_NOT_FOUND = "SESSION_ORDER_NOT_FOUND";

    // Invoice / Payment
    public static final String INVOICE_NOT_FOUND = "INVOICE_NOT_FOUND";
    public static final String INVOICE_ALREADY_PAID = "INVOICE_ALREADY_PAID";
    public static final String INVALID_DISCOUNT_PERCENT = "INVALID_DISCOUNT_PERCENT";
}
