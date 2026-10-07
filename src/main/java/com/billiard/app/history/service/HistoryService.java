package com.billiard.app.history.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.ErrorCode;
import com.billiard.app.common.exception.ValidationException;
import com.billiard.app.history.dto.SessionHistoryResponse;
import com.billiard.app.invoice.dto.InvoiceResponse;
import com.billiard.app.invoice.mapper.InvoiceMapper;
import com.billiard.app.invoice.repository.InvoiceRepository;
import com.billiard.app.table.entity.BilliardTable;
import com.billiard.app.table.repository.TableRepository;
import com.billiard.app.tablesession.entity.TableSession;
import com.billiard.app.tablesession.entity.TableSessionStatus;
import com.billiard.app.tablesession.repository.TableSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class HistoryService {

    private final TableSessionRepository tableSessionRepository;
    private final TableRepository tableRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceMapper invoiceMapper;
    private final CurrentShopProvider currentShopProvider;

    public HistoryService(TableSessionRepository tableSessionRepository,
                           TableRepository tableRepository,
                           InvoiceRepository invoiceRepository,
                           InvoiceMapper invoiceMapper,
                           CurrentShopProvider currentShopProvider) {
        this.tableSessionRepository = tableSessionRepository;
        this.tableRepository = tableRepository;
        this.invoiceRepository = invoiceRepository;
        this.invoiceMapper = invoiceMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public List<SessionHistoryResponse> listClosedSessions(LocalDate from, LocalDate to) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Instant[] range = resolveRange(from, to);

        List<TableSession> sessions = tableSessionRepository
                .findAllByShopIdAndStatusAndStartTimeBetweenOrderByStartTimeDesc(
                        shopId, TableSessionStatus.CLOSED, range[0], range[1]);

        Map<UUID, String> tableNumbersById = new HashMap<>();
        for (BilliardTable table : tableRepository.findAllByShopIdOrderByTableNumberAsc(shopId)) {
            tableNumbersById.put(table.getId(), table.getTableNumber());
        }

        return sessions.stream()
                .map(session -> new SessionHistoryResponse(
                        session.getId(),
                        session.getTableId(),
                        tableNumbersById.get(session.getTableId()),
                        session.getStartTime(),
                        session.getEndTime(),
                        session.getPricePerHourSnapshot()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> listInvoices(LocalDate from, LocalDate to) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Instant[] range = resolveRange(from, to);

        return invoiceRepository
                .findAllByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(shopId, range[0], range[1])
                .stream()
                .map(invoiceMapper::toResponse)
                .toList();
    }

    private Instant[] resolveRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ValidationException(ErrorCode.VALIDATION_ERROR, "'from' date must not be after 'to' date");
        }
        Instant fromInstant = from != null
                ? from.atStartOfDay(ZoneOffset.UTC).toInstant()
                : Instant.EPOCH;
        Instant toInstant = to != null
                ? to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                : Instant.now().plusSeconds(1);
        return new Instant[]{fromInstant, toInstant};
    }
}
