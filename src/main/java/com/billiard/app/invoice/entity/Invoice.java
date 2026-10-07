package com.billiard.app.invoice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    private UUID id;

    @Column(name = "shop_id", nullable = false)
    private UUID shopId;

    @Column(name = "table_session_id", nullable = false)
    private UUID tableSessionId;

    /** Table (playtime) amount before discount. */
    @Column(name = "table_amount", nullable = false)
    private BigDecimal tableAmount;

    @Column(name = "discount_percent", nullable = false)
    private int discountPercent;

    /** = tableAmount * (1 - discountPercent / 100). */
    @Column(name = "table_amount_after_discount", nullable = false)
    private BigDecimal tableAmountAfterDiscount;

    /** Sum of session_orders line_total - never discounted. */
    @Column(name = "food_drink_amount", nullable = false)
    private BigDecimal foodDrinkAmount;

    /** = tableAmountAfterDiscount + foodDrinkAmount. */
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = InvoiceStatus.UNPAID;
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
