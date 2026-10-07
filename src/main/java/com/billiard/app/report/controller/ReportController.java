package com.billiard.app.report.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.report.dto.ReportPeriod;
import com.billiard.app.report.dto.ReportSummaryResponse;
import com.billiard.app.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ApiResponse<ReportSummaryResponse> getSummary(
            @RequestParam(defaultValue = "LAST_7_DAYS") ReportPeriod period) {
        return ApiResponse.success(reportService.getSummary(period));
    }
}
