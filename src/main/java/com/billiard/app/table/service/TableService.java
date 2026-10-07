package com.billiard.app.table.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.ErrorCode;
import com.billiard.app.common.exception.TableAlreadyOccupiedException;
import com.billiard.app.common.exception.TableNotFoundException;
import com.billiard.app.common.exception.ValidationException;
import com.billiard.app.table.dto.CreateTableRequest;
import com.billiard.app.table.dto.TableResponse;
import com.billiard.app.table.dto.UpdateTableRequest;
import com.billiard.app.table.entity.BilliardTable;
import com.billiard.app.table.entity.TableStatus;
import com.billiard.app.table.mapper.TableMapper;
import com.billiard.app.table.repository.TableRepository;
import com.billiard.app.tablesession.entity.TableSession;
import com.billiard.app.tablesession.entity.TableSessionStatus;
import com.billiard.app.tablesession.repository.TableSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TableService {

    private static final Logger log = LoggerFactory.getLogger(TableService.class);

    private final TableRepository tableRepository;
    private final TableSessionRepository tableSessionRepository;
    private final TableMapper tableMapper;
    private final CurrentShopProvider currentShopProvider;

    public TableService(TableRepository tableRepository,
                         TableSessionRepository tableSessionRepository,
                         TableMapper tableMapper,
                         CurrentShopProvider currentShopProvider) {
        this.tableRepository = tableRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.tableMapper = tableMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public List<TableResponse> listTables() {
        UUID shopId = currentShopProvider.getCurrentShopId();

        Map<UUID, TableSession> activeSessionByTableId = tableSessionRepository
                .findAllByShopIdAndStatus(shopId, TableSessionStatus.ACTIVE).stream()
                .collect(Collectors.toMap(TableSession::getTableId, Function.identity()));

        return tableRepository.findAllByShopIdOrderByTableNumberAsc(shopId).stream()
                .map(table -> tableMapper.toResponse(table, activeSessionByTableId.get(table.getId())))
                .toList();
    }

    @Transactional
    public TableResponse createTable(CreateTableRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        if (tableRepository.existsByShopIdAndTableNumber(shopId, request.tableNumber())) {
            throw new ValidationException(ErrorCode.TABLE_NUMBER_ALREADY_EXISTS,
                    "Table number already exists: " + request.tableNumber());
        }

        BilliardTable table = BilliardTable.builder()
                .shopId(shopId)
                .tableNumber(request.tableNumber())
                .tableType(request.tableType())
                .pricePerHour(request.pricePerHour() != null
                        ? request.pricePerHour()
                        : BilliardTable.DEFAULT_PRICE_PER_HOUR)
                .status(TableStatus.AVAILABLE)
                .build();
        table = tableRepository.save(table);
        log.info("Created table: tableId={}, shopId={}", table.getId(), shopId);
        return tableMapper.toResponse(table);
    }

    @Transactional
    public TableResponse updateTable(UUID tableId, UpdateTableRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        BilliardTable table = getOwnedTable(tableId, shopId);

        if (tableRepository.existsByShopIdAndTableNumberAndIdNot(shopId, request.tableNumber(), tableId)) {
            throw new ValidationException(ErrorCode.TABLE_NUMBER_ALREADY_EXISTS,
                    "Table number already exists: " + request.tableNumber());
        }

        // Note (UC-03): price changes are not retroactive - an already-open
        // session keeps its own price snapshot taken at open time.
        table.setTableNumber(request.tableNumber());
        table.setTableType(request.tableType());
        table.setPricePerHour(request.pricePerHour());
        table = tableRepository.save(table);
        log.info("Updated table: tableId={}, shopId={}", table.getId(), shopId);
        return tableMapper.toResponse(table);
    }

    @Transactional
    public TableResponse deactivateTable(UUID tableId) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        BilliardTable table = getOwnedTable(tableId, shopId);

        if (table.getStatus() == TableStatus.OCCUPIED) {
            throw new TableAlreadyOccupiedException(
                    "Cannot deactivate an occupied table; close it first: " + tableId);
        }

        table.setStatus(TableStatus.MAINTENANCE);
        table = tableRepository.save(table);
        log.info("Deactivated table: tableId={}, shopId={}", table.getId(), shopId);
        return tableMapper.toResponse(table);
    }

    private BilliardTable getOwnedTable(UUID tableId, UUID shopId) {
        return tableRepository.findByIdAndShopId(tableId, shopId)
                .orElseThrow(() -> new TableNotFoundException(tableId));
    }
}
