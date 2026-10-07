package com.billiard.app.tablesession.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.TableAlreadyOccupiedException;
import com.billiard.app.common.exception.TableNotAvailableException;
import com.billiard.app.common.exception.TableNotFoundException;
import com.billiard.app.common.exception.TableSessionAlreadyClosedException;
import com.billiard.app.common.exception.TableSessionNotFoundException;
import com.billiard.app.invoice.dto.InvoiceResponse;
import com.billiard.app.invoice.entity.Invoice;
import com.billiard.app.invoice.entity.InvoiceStatus;
import com.billiard.app.invoice.mapper.InvoiceMapper;
import com.billiard.app.invoice.repository.InvoiceRepository;
import com.billiard.app.sessionorder.repository.SessionOrderRepository;
import com.billiard.app.table.entity.BilliardTable;
import com.billiard.app.table.entity.TableStatus;
import com.billiard.app.table.repository.TableRepository;
import com.billiard.app.tablesession.dto.CloseTableSessionResponse;
import com.billiard.app.tablesession.dto.OpenTableSessionRequest;
import com.billiard.app.tablesession.dto.TableSessionResponse;
import com.billiard.app.tablesession.entity.TableSession;
import com.billiard.app.tablesession.entity.TableSessionStatus;
import com.billiard.app.tablesession.mapper.TableSessionMapper;
import com.billiard.app.tablesession.repository.TableSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TableSessionService {

    private static final Logger log = LoggerFactory.getLogger(TableSessionService.class);
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal SECONDS_PER_HOUR = BigDecimal.valueOf(3600);

    private final TableSessionRepository tableSessionRepository;
    private final TableRepository tableRepository;
    private final InvoiceRepository invoiceRepository;
    private final SessionOrderRepository sessionOrderRepository;
    private final TableSessionMapper tableSessionMapper;
    private final InvoiceMapper invoiceMapper;
    private final CurrentShopProvider currentShopProvider;

    public TableSessionService(TableSessionRepository tableSessionRepository,
                                TableRepository tableRepository,
                                InvoiceRepository invoiceRepository,
                                SessionOrderRepository sessionOrderRepository,
                                TableSessionMapper tableSessionMapper,
                                InvoiceMapper invoiceMapper,
                                CurrentShopProvider currentShopProvider) {
        this.tableSessionRepository = tableSessionRepository;
        this.tableRepository = tableRepository;
        this.invoiceRepository = invoiceRepository;
        this.sessionOrderRepository = sessionOrderRepository;
        this.tableSessionMapper = tableSessionMapper;
        this.invoiceMapper = invoiceMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public TableSessionResponse getSession(UUID sessionId) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        TableSession session = getOwnedSession(sessionId, shopId);
        return tableSessionMapper.toResponse(session);
    }

    /**
     * UC-05: Open table. Table State Machine: AVAILABLE -> OCCUPIED only.
     * Price-per-hour is snapshotted from the table's current price at open
     * time - never trusted from the client.
     */
    @Transactional
    public TableSessionResponse openSession(OpenTableSessionRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();

        BilliardTable table = tableRepository.findByIdAndShopId(request.tableId(), shopId)
                .orElseThrow(() -> new TableNotFoundException(request.tableId()));

        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new TableAlreadyOccupiedException("Table is already occupied: " + table.getId());
        }
        if (table.getStatus() == TableStatus.MAINTENANCE) {
            throw new TableNotAvailableException("Table is under maintenance and cannot be opened: " + table.getId());
        }

        TableSession session = TableSession.builder()
                .shopId(shopId)
                .tableId(table.getId())
                .startTime(Instant.now())
                .pricePerHourSnapshot(table.getPricePerHour())
                .status(TableSessionStatus.ACTIVE)
                .build();

        try {
            session = tableSessionRepository.save(session);
        } catch (DataIntegrityViolationException ex) {
            // Unique partial index (one ACTIVE session per table) caught a race condition.
            throw new TableAlreadyOccupiedException("Table is already occupied: " + table.getId());
        }

        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        log.info("Opened table session: sessionId={}, tableId={}, shopId={}", session.getId(), table.getId(), shopId);
        return tableSessionMapper.toResponse(session);
    }

    /**
     * UC-06: Close table. Computes playtime duration and table_amount from
     * the price snapshot taken at open time, sums food_drink_amount from
     * session_orders, creates a draft UNPAID invoice, closes the session and
     * frees the table - all within a single transaction (technology rules
     * section 18).
     */
    @Transactional
    public CloseTableSessionResponse closeSession(UUID sessionId) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        TableSession session = getOwnedSession(sessionId, shopId);

        if (session.getStatus() == TableSessionStatus.CLOSED) {
            throw new TableSessionAlreadyClosedException("Table session is already closed: " + sessionId);
        }

        Instant endTime = Instant.now();
        Duration playDuration = Duration.between(session.getStartTime(), endTime);
        BigDecimal hoursPlayed = BigDecimal.valueOf(Math.max(playDuration.getSeconds(), 0))
                .divide(SECONDS_PER_HOUR, 6, RoundingMode.HALF_UP);
        BigDecimal tableAmount = session.getPricePerHourSnapshot()
                .multiply(hoursPlayed)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        BigDecimal foodDrinkAmount = sessionOrderRepository
                .sumLineTotalByTableSessionIdAndShopId(sessionId, shopId)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        session.setEndTime(endTime);
        session.setStatus(TableSessionStatus.CLOSED);
        session = tableSessionRepository.save(session);

        Invoice invoice = Invoice.builder()
                .shopId(shopId)
                .tableSessionId(session.getId())
                .tableAmount(tableAmount)
                .discountPercent(0)
                .tableAmountAfterDiscount(tableAmount)
                .foodDrinkAmount(foodDrinkAmount)
                .totalAmount(tableAmount.add(foodDrinkAmount))
                .status(InvoiceStatus.UNPAID)
                .build();
        invoice = invoiceRepository.save(invoice);

        UUID tableId = session.getTableId();
        BilliardTable table = tableRepository.findByIdAndShopId(tableId, shopId)
                .orElseThrow(() -> new TableNotFoundException(tableId));
        table.setStatus(TableStatus.AVAILABLE);
        tableRepository.save(table);

        log.info("Closed table session: sessionId={}, tableId={}, tableAmount={}, foodDrinkAmount={}, invoiceId={}, shopId={}",
                sessionId, table.getId(), tableAmount, foodDrinkAmount, invoice.getId(), shopId);

        TableSessionResponse sessionResponse = tableSessionMapper.toResponse(session);
        InvoiceResponse invoiceResponse = invoiceMapper.toResponse(invoice, sessionResponse.orders());
        return new CloseTableSessionResponse(sessionResponse, invoiceResponse);
    }

    private TableSession getOwnedSession(UUID sessionId, UUID shopId) {
        return tableSessionRepository.findByIdAndShopId(sessionId, shopId)
                .orElseThrow(() -> new TableSessionNotFoundException(sessionId));
    }
}
