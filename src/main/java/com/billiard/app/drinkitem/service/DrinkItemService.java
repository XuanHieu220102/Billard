package com.billiard.app.drinkitem.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.DrinkItemNotFoundException;
import com.billiard.app.drinkitem.dto.CreateDrinkItemRequest;
import com.billiard.app.drinkitem.dto.DrinkItemResponse;
import com.billiard.app.drinkitem.dto.StockEntryRequest;
import com.billiard.app.drinkitem.dto.UpdateDrinkItemRequest;
import com.billiard.app.drinkitem.entity.DrinkItem;
import com.billiard.app.drinkitem.entity.DrinkStockEntry;
import com.billiard.app.drinkitem.mapper.DrinkItemMapper;
import com.billiard.app.drinkitem.repository.DrinkItemRepository;
import com.billiard.app.drinkitem.repository.DrinkStockEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DrinkItemService {

    private static final Logger log = LoggerFactory.getLogger(DrinkItemService.class);

    private final DrinkItemRepository drinkItemRepository;
    private final DrinkStockEntryRepository drinkStockEntryRepository;
    private final DrinkItemMapper drinkItemMapper;
    private final CurrentShopProvider currentShopProvider;

    public DrinkItemService(DrinkItemRepository drinkItemRepository,
                             DrinkStockEntryRepository drinkStockEntryRepository,
                             DrinkItemMapper drinkItemMapper,
                             CurrentShopProvider currentShopProvider) {
        this.drinkItemRepository = drinkItemRepository;
        this.drinkStockEntryRepository = drinkStockEntryRepository;
        this.drinkItemMapper = drinkItemMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public List<DrinkItemResponse> listDrinkItems() {
        UUID shopId = currentShopProvider.getCurrentShopId();
        return drinkItemRepository.findAllByShopIdOrderByNameAsc(shopId).stream()
                .map(drinkItemMapper::toResponse)
                .toList();
    }

    @Transactional
    public DrinkItemResponse createDrinkItem(CreateDrinkItemRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        DrinkItem item = DrinkItem.builder()
                .shopId(shopId)
                .name(request.name())
                .price(request.price())
                .build();
        item = drinkItemRepository.save(item);
        log.info("Created drink item: id={}, shopId={}", item.getId(), shopId);
        return drinkItemMapper.toResponse(item);
    }

    @Transactional
    public DrinkItemResponse updateDrinkItem(UUID id, UpdateDrinkItemRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        DrinkItem item = getOwnedDrinkItem(id, shopId);
        item.setName(request.name());
        item.setPrice(request.price());
        item = drinkItemRepository.save(item);
        log.info("Updated drink item: id={}, shopId={}", item.getId(), shopId);
        return drinkItemMapper.toResponse(item);
    }

    @Transactional
    public DrinkItemResponse deactivateDrinkItem(UUID id) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        DrinkItem item = getOwnedDrinkItem(id, shopId);
        item.setActive(false);
        item = drinkItemRepository.save(item);
        log.info("Deactivated drink item: id={}, shopId={}", item.getId(), shopId);
        return drinkItemMapper.toResponse(item);
    }

    @Transactional
    public DrinkItemResponse addStockEntry(UUID id, StockEntryRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        UUID userId = currentShopProvider.getCurrentUserId();

        DrinkItem item = drinkItemRepository.findByIdAndShopIdForUpdate(id, shopId)
                .orElseThrow(() -> new DrinkItemNotFoundException(id));

        item.setStockQuantity(item.getStockQuantity() + request.quantityAdded());
        item = drinkItemRepository.save(item);

        DrinkStockEntry entry = DrinkStockEntry.builder()
                .shopId(shopId)
                .drinkItemId(id)
                .quantityAdded(request.quantityAdded())
                .createdBy(userId)
                .build();
        drinkStockEntryRepository.save(entry);

        log.info("Added stock entry: drinkItemId={}, quantityAdded={}, newStock={}, shopId={}",
                id, request.quantityAdded(), item.getStockQuantity(), shopId);
        return drinkItemMapper.toResponse(item);
    }

    private DrinkItem getOwnedDrinkItem(UUID id, UUID shopId) {
        return drinkItemRepository.findByIdAndShopId(id, shopId)
                .orElseThrow(() -> new DrinkItemNotFoundException(id));
    }
}
