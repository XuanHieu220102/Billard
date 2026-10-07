package com.billiard.app.report.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.invoice.entity.Invoice;
import com.billiard.app.invoice.entity.InvoiceStatus;
import com.billiard.app.invoice.repository.InvoiceRepository;
import com.billiard.app.report.dto.ReportPeriod;
import com.billiard.app.report.dto.ReportSummaryResponse;
import com.billiard.app.sessionorder.entity.SessionOrder;
import com.billiard.app.sessionorder.repository.SessionOrderRepository;
import com.billiard.app.tablesession.entity.TableSession;
import com.billiard.app.tablesession.entity.TableSessionStatus;
import com.billiard.app.tablesession.repository.TableSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final int TOP_ITEMS_LIMIT = 5;

    private final TableSessionRepository tableSessionRepository;
    private final InvoiceRepository invoiceRepository;
    private final SessionOrderRepository sessionOrderRepository;
    private final CurrentShopProvider currentShopProvider;

    public ReportService(TableSessionRepository tableSessionRepository,
                          InvoiceRepository invoiceRepository,
                          SessionOrderRepository sessionOrderRepository,
                          CurrentShopProvider currentShopProvider) {
        this.tableSessionRepository = tableSessionRepository;
        this.invoiceRepository = invoiceRepository;
        this.sessionOrderRepository = sessionOrderRepository;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse getSummary(ReportPeriod period) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Instant[] range = resolveRange(period);

        List<TableSession> closedSessions = tableSessionRepository
                .findAllByShopIdAndStatusAndStartTimeBetweenOrderByStartTimeDesc(
                        shopId, TableSessionStatus.CLOSED, range[0], range[1]);

        // "Doanh thu" chỉ tính tiền đã thực thu — bỏ qua hóa đơn UNPAID (nếu có) để
        // nhất quán giữa Dashboard và trang Báo cáo.
        List<Invoice> paidInvoices = invoiceRepository
                .findAllByShopIdAndCreatedAtBetweenOrderByCreatedAtDesc(shopId, range[0], range[1])
                .stream()
                .filter(invoice -> invoice.getStatus() == InvoiceStatus.PAID)
                .toList();

        BigDecimal tableRevenue = paidInvoices.stream()
                .map(Invoice::getTableAmountAfterDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal foodDrinkRevenue = paidInvoices.stream()
                .map(Invoice::getFoodDrinkAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRevenue = tableRevenue.add(foodDrinkRevenue);

        List<ReportSummaryResponse.HourlySessionCount> sessionsByHour = buildHourlySessionCounts(closedSessions);
        List<ReportSummaryResponse.TopFoodDrinkItem> topItems =
                buildTopFoodDrinkItems(shopId, closedSessions, foodDrinkRevenue);

        long sessionsStartedCount = tableSessionRepository
                .countByShopIdAndStartTimeBetween(shopId, range[0], range[1]);

        return new ReportSummaryResponse(
                totalRevenue,
                tableRevenue,
                foodDrinkRevenue,
                closedSessions.size(),
                sessionsStartedCount,
                sessionsByHour,
                topItems
        );
    }

    private List<ReportSummaryResponse.HourlySessionCount> buildHourlySessionCounts(List<TableSession> sessions) {
        Map<Integer, Long> countByHour = sessions.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getStartTime().atZone(ZoneOffset.UTC).getHour(),
                        Collectors.counting()
                ));

        Map<Integer, Long> ordered = new LinkedHashMap<>();
        for (int hour = 0; hour < 24; hour++) {
            ordered.put(hour, countByHour.getOrDefault(hour, 0L));
        }
        return ordered.entrySet().stream()
                .map(e -> new ReportSummaryResponse.HourlySessionCount(e.getKey(), e.getValue()))
                .toList();
    }

    private List<ReportSummaryResponse.TopFoodDrinkItem> buildTopFoodDrinkItems(
            UUID shopId, List<TableSession> closedSessions, BigDecimal foodDrinkRevenue) {
        if (closedSessions.isEmpty()) {
            return List.of();
        }

        List<UUID> sessionIds = closedSessions.stream().map(TableSession::getId).toList();
        List<SessionOrder> orders = sessionOrderRepository.findAllByShopIdAndTableSessionIdIn(shopId, sessionIds);

        record ItemAggregate(String itemName, long quantity, BigDecimal revenue) {
        }

        Map<UUID, ItemAggregate> aggregateByItemId = new LinkedHashMap<>();
        for (SessionOrder order : orders) {
            aggregateByItemId.merge(
                    order.getItemId(),
                    new ItemAggregate(order.getItemName(), order.getQuantity(), order.getLineTotal()),
                    (existing, incoming) -> new ItemAggregate(
                            incoming.itemName(),
                            existing.quantity() + incoming.quantity(),
                            existing.revenue().add(incoming.revenue())
                    )
            );
        }

        return aggregateByItemId.values().stream()
                .sorted(Comparator.comparing(ItemAggregate::revenue).reversed())
                .limit(TOP_ITEMS_LIMIT)
                .map(agg -> new ReportSummaryResponse.TopFoodDrinkItem(
                        agg.itemName(),
                        agg.quantity(),
                        agg.revenue(),
                        sharePercent(agg.revenue(), foodDrinkRevenue)
                ))
                .toList();
    }

    private BigDecimal sharePercent(BigDecimal part, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return part.multiply(BigDecimal.valueOf(100))
                .divide(total, 1, RoundingMode.HALF_UP);
    }

    private Instant[] resolveRange(ReportPeriod period) {
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        LocalDate today = now.toLocalDate();

        LocalDate fromDate = switch (period) {
            case TODAY -> today;
            case LAST_7_DAYS -> today.minusDays(6);
            case THIS_MONTH -> today.withDayOfMonth(1);
        };

        Instant from = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = now.toInstant();
        return new Instant[]{from, to};
    }
}
