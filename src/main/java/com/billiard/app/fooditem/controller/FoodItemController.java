package com.billiard.app.fooditem.controller;

import com.billiard.app.common.response.ApiResponse;
import com.billiard.app.fooditem.dto.CreateFoodItemRequest;
import com.billiard.app.fooditem.dto.FoodItemResponse;
import com.billiard.app.fooditem.dto.UpdateFoodItemRequest;
import com.billiard.app.fooditem.entity.FoodItemCategory;
import com.billiard.app.fooditem.service.FoodItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/food-items")
public class FoodItemController {

    private final FoodItemService foodItemService;

    public FoodItemController(FoodItemService foodItemService) {
        this.foodItemService = foodItemService;
    }

    @GetMapping
    public ApiResponse<List<FoodItemResponse>> listFoodItems(
            @RequestParam(required = false) FoodItemCategory category) {
        return ApiResponse.success(foodItemService.listFoodItems(category));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<FoodItemResponse> createFoodItem(@Valid @RequestBody CreateFoodItemRequest request) {
        return ApiResponse.success(foodItemService.createFoodItem(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<FoodItemResponse> updateFoodItem(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateFoodItemRequest request) {
        return ApiResponse.success(foodItemService.updateFoodItem(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    public ApiResponse<FoodItemResponse> deactivateFoodItem(@PathVariable UUID id) {
        return ApiResponse.success(foodItemService.deactivateFoodItem(id));
    }
}
