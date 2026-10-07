package com.billiard.app.invoice.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.invoice.dto.ApplyDiscountRequest;
import com.billiard.app.invoice.dto.InvoiceResponse;
import com.billiard.app.invoice.dto.PayInvoiceRequest;
import com.billiard.app.invoice.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getInvoice(@PathVariable UUID id) {
        return ApiResponse.success(invoiceService.getInvoice(id));
    }

    @PatchMapping("/{id}/discount")
    public ApiResponse<InvoiceResponse> applyDiscount(@PathVariable UUID id,
                                                        @Valid @RequestBody ApplyDiscountRequest request) {
        return ApiResponse.success(invoiceService.applyDiscount(id, request));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<InvoiceResponse> payInvoice(@PathVariable UUID id,
                                                     @Valid @RequestBody PayInvoiceRequest request) {
        return ApiResponse.success(invoiceService.payInvoice(id, request));
    }
}
