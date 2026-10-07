package com.billiard.app.invoice.mapper;

import com.billiard.app.invoice.dto.InvoiceResponse;
import com.billiard.app.invoice.entity.Invoice;
import com.billiard.app.sessionorder.dto.SessionOrderResponse;
import com.billiard.app.sessionorder.mapper.SessionOrderMapper;
import com.billiard.app.sessionorder.repository.SessionOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InvoiceMapper {

    private final SessionOrderRepository sessionOrderRepository;
    private final SessionOrderMapper sessionOrderMapper;

    public InvoiceMapper(SessionOrderRepository sessionOrderRepository, SessionOrderMapper sessionOrderMapper) {
        this.sessionOrderRepository = sessionOrderRepository;
        this.sessionOrderMapper = sessionOrderMapper;
    }

    public InvoiceResponse toResponse(Invoice invoice) {
        List<SessionOrderResponse> lineItems = sessionOrderRepository
                .findAllByTableSessionIdAndShopIdOrderByCreatedAtAsc(invoice.getTableSessionId(), invoice.getShopId())
                .stream()
                .map(sessionOrderMapper::toResponse)
                .toList();
        return toResponse(invoice, lineItems);
    }

    public InvoiceResponse toResponse(Invoice invoice, List<SessionOrderResponse> lineItems) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getTableSessionId(),
                invoice.getTableAmount(),
                invoice.getDiscountPercent(),
                invoice.getTableAmountAfterDiscount(),
                invoice.getFoodDrinkAmount(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                lineItems,
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
