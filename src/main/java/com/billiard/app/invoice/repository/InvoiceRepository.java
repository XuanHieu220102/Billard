package com.billiard.app.invoice.repository;

import com.billiard.app.invoice.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByIdAndShopId(UUID id, UUID shopId);

    Optional<Invoice> findByTableSessionIdAndShopId(UUID tableSessionId, UUID shopId);

    List<Invoice> findAllByShopIdOrderByCreatedAtDesc(UUID shopId);

    List<Invoice> findAllByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(UUID shopId, Instant from, Instant to);
}
