package com.billiard.app.fooditem.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.FoodItemNotFoundException;
import com.billiard.app.fooditem.dto.CreateFoodItemRequest;
import com.billiard.app.fooditem.dto.FoodItemResponse;
import com.billiard.app.fooditem.dto.UpdateFoodItemRequest;
import com.billiard.app.fooditem.entity.FoodItem;
import com.billiard.app.fooditem.entity.FoodItemCategory;
import com.billiard.app.fooditem.mapper.FoodItemMapper;
import com.billiard.app.fooditem.repository.FoodItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FoodItemService {

    private static final Logger log = LoggerFactory.getLogger(FoodItemService.class);

    private final FoodItemRepository foodItemRepository;
    private final FoodItemMapper foodItemMapper;
    private final CurrentShopProvider currentShopProvider;

    public FoodItemService(FoodItemRepository foodItemRepository,
                            FoodItemMapper foodItemMapper,
                            CurrentShopProvider currentShopProvider) {
        this.foodItemRepository = foodItemRepository;
        this.foodItemMapper = foodItemMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public List<FoodItemResponse> listFoodItems(FoodItemCategory category) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        List<FoodItem> items = category != null
                ? foodItemRepository.findAllByShopIdAndCategoryOrderByNameAsc(shopId, category)
                : foodItemRepository.findAllByShopIdOrderByNameAsc(shopId);
        return items.stream()
                .map(foodItemMapper::toResponse)
                .toList();
    }

    @Transactional
    public FoodItemResponse createFoodItem(CreateFoodItemRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        FoodItem item = FoodItem.builder()
                .shopId(shopId)
                .name(request.name())
                .price(request.price())
                .category(request.category())
                .build();
        item = foodItemRepository.save(item);
        log.info("Created food item: id={}, shopId={}, category={}", item.getId(), shopId, item.getCategory());
        return foodItemMapper.toResponse(item);
    }

    @Transactional
    public FoodItemResponse updateFoodItem(UUID id, UpdateFoodItemRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        FoodItem item = getOwnedFoodItem(id, shopId);
        item.setName(request.name());
        item.setPrice(request.price());
        item = foodItemRepository.save(item);
        log.info("Updated food item: id={}, shopId={}", item.getId(), shopId);
        return foodItemMapper.toResponse(item);
    }

    @Transactional
    public FoodItemResponse deactivateFoodItem(UUID id) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        FoodItem item = getOwnedFoodItem(id, shopId);
        item.setActive(false);
        item = foodItemRepository.save(item);
        log.info("Deactivated food item: id={}, shopId={}", item.getId(), shopId);
        return foodItemMapper.toResponse(item);
    }

    private FoodItem getOwnedFoodItem(UUID id, UUID shopId) {
        return foodItemRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new FoodItemNotFoundException(id));
    }
}
