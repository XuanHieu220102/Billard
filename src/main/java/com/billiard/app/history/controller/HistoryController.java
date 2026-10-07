package com.billiard.app.history.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.history.dto.SessionHistoryResponse;
import com.billiard.app.history.service.HistoryService;
import com.billiard.app.invoice.dto.InvoiceResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SessionHistoryResponse>> listSessions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(historyService.listClosedSessions(from, to));
    }

    @GetMapping("/invoices")
    public ApiResponse<List<InvoiceResponse>> listInvoices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(historyService.listInvoices(from, to));
    }
}
