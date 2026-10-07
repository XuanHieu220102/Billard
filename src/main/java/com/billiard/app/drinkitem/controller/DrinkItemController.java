package com.billiard.app.drinkitem.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.drinkitem.dto.CreateDrinkItemRequest;
import com.billiard.app.drinkitem.dto.DrinkItemResponse;
import com.billiard.app.drinkitem.dto.StockEntryRequest;
import com.billiard.app.drinkitem.dto.UpdateDrinkItemRequest;
import com.billiard.app.drinkitem.service.DrinkItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drink-items")
public class DrinkItemController {

    private final DrinkItemService drinkItemService;

    public DrinkItemController(DrinkItemService drinkItemService) {
        this.drinkItemService = drinkItemService;
    }

    @GetMapping
    public ApiResponse<List<DrinkItemResponse>> listDrinkItems() {
        return ApiResponse.success(drinkItemService.listDrinkItems());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DrinkItemResponse> createDrinkItem(@Valid @RequestBody CreateDrinkItemRequest request) {
        return ApiResponse.success(drinkItemService.createDrinkItem(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<DrinkItemResponse> updateDrinkItem(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateDrinkItemRequest request) {
        return ApiResponse.success(drinkItemService.updateDrinkItem(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<DrinkItemResponse> deactivateDrinkItem(@PathVariable UUID id) {
        return ApiResponse.success(drinkItemService.deactivateDrinkItem(id));
    }

    @PostMapping("/{id}/stock-entries")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<DrinkItemResponse> addStockEntry(@PathVariable UUID id,
                                                          @Valid @RequestBody StockEntryRequest request) {
        return ApiResponse.success(drinkItemService.addStockEntry(id, request));
    }
}
