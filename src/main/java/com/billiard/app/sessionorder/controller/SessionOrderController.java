package com.billiard.app.sessionorder.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.sessionorder.dto.CreateSessionOrderRequest;
import com.billiard.app.sessionorder.dto.SessionOrderResponse;
import com.billiard.app.sessionorder.dto.UpdateSessionOrderRequest;
import com.billiard.app.sessionorder.service.SessionOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/table-sessions/{sessionId}/orders")
public class SessionOrderController {

    private final SessionOrderService sessionOrderService;

    public SessionOrderController(SessionOrderService sessionOrderService) {
        this.sessionOrderService = sessionOrderService;
    }

    @GetMapping
    public ApiResponse<List<SessionOrderResponse>> listOrders(@PathVariable UUID sessionId) {
        return ApiResponse.success(sessionOrderService.listOrders(sessionId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SessionOrderResponse> addOrder(@PathVariable UUID sessionId,
                                                        @Valid @RequestBody CreateSessionOrderRequest request) {
        return ApiResponse.success(sessionOrderService.addOrder(sessionId, request));
    }

    @PatchMapping("/{orderId}")
    public ApiResponse<SessionOrderResponse> updateOrderQuantity(@PathVariable UUID sessionId,
                                                                   @PathVariable UUID orderId,
                                                                   @Valid @RequestBody UpdateSessionOrderRequest request) {
        return ApiResponse.success(sessionOrderService.updateOrderQuantity(sessionId, orderId, request));
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable UUID sessionId, @PathVariable UUID orderId) {
        sessionOrderService.deleteOrder(sessionId, orderId);
    }
}
