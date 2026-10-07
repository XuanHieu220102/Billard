package com.billiard.app.table.mapper;

import com.billiard.app.table.dto.TableResponse;
import com.billiard.app.table.entity.BilliardTable;
import com.billiard.app.tablesession.entity.TableSession;
import org.springframework.stereotype.Component;

@Component
public class TableMapper {

    public TableResponse toResponse(BilliardTable table) {
        return toResponse(table, null);
    }

    /**
     * @param activeSession the table's currently ACTIVE session, or null if the table
     *                      is not OCCUPIED. Used by the Dashboard (UC-01) to show the
     *                      elapsed-time counter without a separate API call per table.
     */
    public TableResponse toResponse(BilliardTable table, TableSession activeSession) {
        return new TableResponse(
                table.getId(),
                table.getTableNumber(),
                table.getTableType(),
                table.getPricePerHour(),
                table.getStatus(),
                activeSession != null ? activeSession.getId() : null,
                activeSession != null ? activeSession.getStartTime() : null,
                table.getCreatedAt(),
                table.getUpdatedAt()
        );
    }
}
