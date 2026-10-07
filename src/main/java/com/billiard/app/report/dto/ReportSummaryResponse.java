package com.billiard.app.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReportSummaryResponse(
        BigDecimal totalRevenue,
        BigDecimal tableRevenue,
        BigDecimal foodDrinkRevenue,
        long totalSessions,
        /** Số phiên đã bắt đầu trong kỳ, gồm cả ACTIVE lẫn CLOSED (khác totalSessions - chỉ đếm CLOSED). */
        long sessionsStartedCount,
        List<HourlySessionCount> sessionsByHour,
        List<TopFoodDrinkItem> topFoodDrinkItems
) {
    public record HourlySessionCount(
            int hourOfDay,
            long sessionCount
    ) {
    }

    public record TopFoodDrinkItem(
            String itemName,
            long totalQuantity,
            BigDecimal totalRevenue,
            BigDecimal revenueSharePercent
    ) {
    }
}
