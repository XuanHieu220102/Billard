package com.billiard.app.invoice.entity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pre-configured discount tiers applicable only to table_amount (UC-19).
 * Free-form percentages are out of MVP scope - see technology rules section 36.
 */
public enum DiscountPercent {
    NONE(0),
    TEN(10),
    THIRTY(30),
    FIFTY(50);

    private final int value;

    DiscountPercent(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isValid(int value) {
        return Arrays.stream(values()).anyMatch(d -> d.value == value);
    }

    public static Set<Integer> allowedValues() {
        return Arrays.stream(values()).map(d -> d.value).collect(Collectors.toSet());
    }
}
