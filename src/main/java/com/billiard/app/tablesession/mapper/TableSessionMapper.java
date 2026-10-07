package com.billiard.app.tablesession.mapper;

import com.billiard.app.sessionorder.dto.SessionOrderResponse;
import com.billiard.app.sessionorder.mapper.SessionOrderMapper;
import com.billiard.app.sessionorder.repository.SessionOrderRepository;
import com.billiard.app.tablesession.dto.TableSessionResponse;
import com.billiard.app.tablesession.entity.TableSession;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TableSessionMapper {

    private final SessionOrderRepository sessionOrderRepository;
    private final SessionOrderMapper sessionOrderMapper;

    public TableSessionMapper(SessionOrderRepository sessionOrderRepository, SessionOrderMapper sessionOrderMapper) {
        this.sessionOrderRepository = sessionOrderRepository;
        this.sessionOrderMapper = sessionOrderMapper;
    }

    public TableSessionResponse toResponse(TableSession session) {
        List<SessionOrderResponse> orders = sessionOrderRepository
                .findAllByTableSessionIdAndShopIdOrderByCreatedAtAsc(session.getId(), session.getShopId())
                .stream()
                .map(sessionOrderMapper::toResponse)
                .toList();
        return toResponse(session, orders);
    }

    public TableSessionResponse toResponse(TableSession session, List<SessionOrderResponse> orders) {
        return new TableSessionResponse(
                session.getId(),
                session.getTableId(),
                session.getStartTime(),
                session.getEndTime(),
                session.getPricePerHourSnapshot(),
                session.getStatus(),
                orders,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
