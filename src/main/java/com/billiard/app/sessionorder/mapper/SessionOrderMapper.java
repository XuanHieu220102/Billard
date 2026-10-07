package com.billiard.app.sessionorder.mapper;

import com.billiard.app.sessionorder.dto.SessionOrderResponse;
import com.billiard.app.sessionorder.entity.SessionOrder;
import org.springframework.stereotype.Component;

@Component
public class SessionOrderMapper {

    public SessionOrderResponse toResponse(SessionOrder order) {
        return new SessionOrderResponse(
                order.getId(),
                order.getTableSessionId(),
                order.getItemType(),
                order.getItemId(),
                order.getItemName(),
                order.getUnitPrice(),
                order.getQuantity(),
                order.getLineTotal(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
