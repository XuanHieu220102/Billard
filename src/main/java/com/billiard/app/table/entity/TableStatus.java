package com.billiard.app.table.entity;

/**
 * Table state machine (see technology rules, section 19):
 * AVAILABLE -> OCCUPIED (open) -> AVAILABLE (close)
 * AVAILABLE -> MAINTENANCE (deactivate)
 * No other transition is allowed.
 */
public enum TableStatus {
    AVAILABLE,
    OCCUPIED,
    MAINTENANCE
}
