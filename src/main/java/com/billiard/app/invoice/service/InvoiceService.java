package com.billiard.app.invoice.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.ErrorCode;
import com.billiard.app.common.exception.InvoiceAlreadyPaidException;
import com.billiard.app.common.exception.InvoiceNotFoundException;
import com.billiard.app.common.exception.ValidationException;
import com.billiard.app.invoice.dto.ApplyDiscountRequest;
import com.billiard.app.invoice.dto.InvoiceResponse;
import com.billiard.app.invoice.dto.PayInvoiceRequest;
import com.billiard.app.invoice.entity.DiscountPercent;
import com.billiard.app.invoice.entity.Invoice;
import com.billiard.app.invoice.entity.InvoiceStatus;
import com.billiard.app.invoice.entity.Payment;
import com.billiard.app.invoice.mapper.InvoiceMapper;
import com.billiard.app.invoice.repository.InvoiceRepository;
import com.billiard.app.invoice.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private static final int MONEY_SCALE = 2;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceMapper invoiceMapper;
    private final CurrentShopProvider currentShopProvider;

    public InvoiceService(InvoiceRepository invoiceRepository,
                           PaymentRepository paymentRepository,
                           InvoiceMapper invoiceMapper,
                           CurrentShopProvider currentShopProvider) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceMapper = invoiceMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(UUID id) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Invoice invoice = getOwnedInvoice(id, shopId);
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse applyDiscount(UUID id, ApplyDiscountRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Invoice invoice = getOwnedInvoice(id, shopId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException("Cannot change discount on a paid invoice: " + id);
        }
        if (!DiscountPercent.isValid(request.discountPercent())) {
            throw new ValidationException(ErrorCode.INVALID_DISCOUNT_PERCENT,
                    "Discount percent must be one of " + DiscountPercent.allowedValues()
                            + ", got: " + request.discountPercent());
        }

        invoice.setDiscountPercent(request.discountPercent());
        invoice.setTableAmountAfterDiscount(applyDiscount(invoice.getTableAmount(), request.discountPercent()));
        invoice.setTotalAmount(invoice.getTableAmountAfterDiscount().add(invoice.getFoodDrinkAmount()));
        invoice = invoiceRepository.save(invoice);

        log.info("Applied discount: invoiceId={}, discountPercent={}, shopId={}",
                id, request.discountPercent(), shopId);
        return invoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse payInvoice(UUID id, PayInvoiceRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        Invoice invoice = getOwnedInvoice(id, shopId);

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException("Invoice is already paid: " + id);
        }

        Instant now = Instant.now();
        Payment payment = Payment.builder()
                .shopId(shopId)
                .invoiceId(invoice.getId())
                .amount(invoice.getTotalAmount())
                .paymentMethod(request.paymentMethod().name())
                .paidAt(now)
                .build();
        paymentRepository.save(payment);

        invoice.setStatus(InvoiceStatus.PAID);
        invoice = invoiceRepository.save(invoice);

        log.info("Paid invoice: invoiceId={}, amount={}, method={}, shopId={}",
                id, invoice.getTotalAmount(), request.paymentMethod(), shopId);
        return invoiceMapper.toResponse(invoice);
    }

    private BigDecimal applyDiscount(BigDecimal amount, int discountPercent) {
        BigDecimal multiplier = BigDecimal.ONE.subtract(
                BigDecimal.valueOf(discountPercent).divide(HUNDRED, 4, RoundingMode.HALF_UP));
        return amount.multiply(multiplier).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private Invoice getOwnedInvoice(UUID id, UUID shopId) {
        return invoiceRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new InvoiceNotFoundException(id));
    }
}
