package com.billiard.app.invoice.entity;

/**
 * [Inference] Exact payment method list was not confirmed in the source
 * documents (see usecase doc section 8, open question #2). CASH and
 * BANK_TRANSFER are used as a reasonable MVP default and can be extended
 * later without a breaking change since the value is stored as a string.
 */
public enum PaymentMethod {
    CASH,
    BANK_TRANSFER
}
