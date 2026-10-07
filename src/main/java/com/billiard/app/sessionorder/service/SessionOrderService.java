package com.billiard.app.sessionorder.service;

import com.billiard.app.auth.security.CurrentShopProvider;
import com.billiard.app.common.exception.DrinkItemNotFoundException;
import com.billiard.app.common.exception.DrinkOutOfStockException;
import com.billiard.app.common.exception.FoodItemNotFoundException;
import com.billiard.app.common.exception.SessionOrderNotFoundException;
import com.billiard.app.common.exception.TableSessionNotActiveException;
import com.billiard.app.common.exception.TableSessionNotFoundException;
import com.billiard.app.drinkitem.entity.DrinkItem;
import com.billiard.app.drinkitem.repository.DrinkItemRepository;
import com.billiard.app.fooditem.entity.FoodItem;
import com.billiard.app.fooditem.repository.FoodItemRepository;
import com.billiard.app.sessionorder.dto.CreateSessionOrderRequest;
import com.billiard.app.sessionorder.dto.SessionOrderResponse;
import com.billiard.app.sessionorder.dto.UpdateSessionOrderRequest;
import com.billiard.app.sessionorder.entity.ItemType;
import com.billiard.app.sessionorder.entity.SessionOrder;
import com.billiard.app.sessionorder.mapper.SessionOrderMapper;
import com.billiard.app.sessionorder.repository.SessionOrderRepository;
import com.billiard.app.tablesession.entity.TableSession;
import com.billiard.app.tablesession.entity.TableSessionStatus;
import com.billiard.app.tablesession.repository.TableSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SessionOrderService {

    private static final Logger log = LoggerFactory.getLogger(SessionOrderService.class);

    private final SessionOrderRepository sessionOrderRepository;
    private final TableSessionRepository tableSessionRepository;
    private final FoodItemRepository foodItemRepository;
    private final DrinkItemRepository drinkItemRepository;
    private final SessionOrderMapper sessionOrderMapper;
    private final CurrentShopProvider currentShopProvider;

    public SessionOrderService(SessionOrderRepository sessionOrderRepository,
                                TableSessionRepository tableSessionRepository,
                                FoodItemRepository foodItemRepository,
                                DrinkItemRepository drinkItemRepository,
                                SessionOrderMapper sessionOrderMapper,
                                CurrentShopProvider currentShopProvider) {
        this.sessionOrderRepository = sessionOrderRepository;
        this.tableSessionRepository = tableSessionRepository;
        this.foodItemRepository = foodItemRepository;
        this.drinkItemRepository = drinkItemRepository;
        this.sessionOrderMapper = sessionOrderMapper;
        this.currentShopProvider = currentShopProvider;
    }

    @Transactional(readOnly = true)
    public List<SessionOrderResponse> listOrders(UUID sessionId) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        getOwnedActiveOrAnySession(sessionId, shopId);
        return sessionOrderRepository.findAllByTableSessionIdAndShopIdOrderByCreatedAtAsc(sessionId, shopId).stream()
                .map(sessionOrderMapper::toResponse)
                .toList();
    }

    @Transactional
    public SessionOrderResponse addOrder(UUID sessionId, CreateSessionOrderRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();
        UUID userId = currentShopProvider.getCurrentUserId();

        TableSession session = getOwnedSession(sessionId, shopId);
        requireActive(session);

        String itemName;
        BigDecimal unitPrice;

        if (request.itemType() == ItemType.DRINK) {
            DrinkItem drinkItem = drinkItemRepository.findByIdAndShopIdForUpdate(request.itemId(), shopId)
                    .orElseThrow(() -> new DrinkItemNotFoundException(request.itemId()));
            if (drinkItem.getStockQuantity() < request.quantity()) {
                throw new DrinkOutOfStockException(
                        "Not enough stock for drink item " + drinkItem.getName()
                                + ": requested=" + request.quantity()
                                + ", available=" + drinkItem.getStockQuantity());
            }
            drinkItem.setStockQuantity(drinkItem.getStockQuantity() - request.quantity());
            drinkItemRepository.save(drinkItem);
            itemName = drinkItem.getName();
            unitPrice = drinkItem.getPrice();
        } else {
            FoodItem foodItem = foodItemRepository.findByIdAndShopId(request.itemId(), shopId)
                    .orElseThrow(() -> new FoodItemNotFoundException(request.itemId()));
            itemName = foodItem.getName();
            unitPrice = foodItem.getPrice();
        }

        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(request.quantity()));

        SessionOrder order = SessionOrder.builder()
                .shopId(shopId)
                .tableSessionId(sessionId)
                .itemType(request.itemType())
                .itemId(request.itemId())
                .itemName(itemName)
                .unitPrice(unitPrice)
                .quantity(request.quantity())
                .lineTotal(lineTotal)
                .createdBy(userId)
                .build();
        order = sessionOrderRepository.save(order);

        log.info("Added session order: orderId={}, sessionId={}, itemType={}, quantity={}, shopId={}",
                order.getId(), sessionId, request.itemType(), request.quantity(), shopId);
        return sessionOrderMapper.toResponse(order);
    }

    @Transactional
    public SessionOrderResponse updateOrderQuantity(UUID sessionId, UUID orderId, UpdateSessionOrderRequest request) {
        UUID shopId = currentShopProvider.getCurrentShopId();

        TableSession session = getOwnedSession(sessionId, shopId);
        requireActive(session);

        SessionOrder order = sessionOrderRepository.findByIdAndTableSessionIdAndShopId(orderId, sessionId, shopId)
                .orElseThrow(() -> new SessionOrderNotFoundException(orderId));

        int oldQuantity = order.getQuantity();
        int newQuantity = request.quantity();
        int delta = newQuantity - oldQuantity;
        UUID orderItemId = order.getItemId();

        if (order.getItemType() == ItemType.DRINK && delta != 0) {
            DrinkItem drinkItem = drinkItemRepository.findByIdAndShopIdForUpdate(orderItemId, shopId)
                    .orElseThrow(() -> new DrinkItemNotFoundException(orderItemId));
            if (delta > 0) {
                if (drinkItem.getStockQuantity() < delta) {
                    throw new DrinkOutOfStockException(
                            "Not enough stock for drink item " + drinkItem.getName()
                                    + " to increase quantity by " + delta
                                    + ": available=" + drinkItem.getStockQuantity());
                }
                drinkItem.setStockQuantity(drinkItem.getStockQuantity() - delta);
            } else {
                drinkItem.setStockQuantity(drinkItem.getStockQuantity() - delta); // delta negative -> stock increases
            }
            drinkItemRepository.save(drinkItem);
        }

        order.setQuantity(newQuantity);
        order.setLineTotal(order.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity)));
        order = sessionOrderRepository.save(order);

        log.info("Updated session order quantity: orderId={}, sessionId={}, oldQuantity={}, newQuantity={}, shopId={}",
                orderId, sessionId, oldQuantity, newQuantity, shopId);
        return sessionOrderMapper.toResponse(order);
    }

    @Transactional
    public void deleteOrder(UUID sessionId, UUID orderId) {
        UUID shopId = currentShopProvider.getCurrentShopId();

        TableSession session = getOwnedSession(sessionId, shopId);
        requireActive(session);

        SessionOrder order = sessionOrderRepository.findByIdAndTableSessionIdAndShopId(orderId, sessionId, shopId)
                .orElseThrow(() -> new SessionOrderNotFoundException(orderId));

        if (order.getItemType() == ItemType.DRINK) {
            DrinkItem drinkItem = drinkItemRepository.findByIdAndShopIdForUpdate(order.getItemId(), shopId)
                    .orElseThrow(() -> new DrinkItemNotFoundException(order.getItemId()));
            drinkItem.setStockQuantity(drinkItem.getStockQuantity() + order.getQuantity());
            drinkItemRepository.save(drinkItem);
        }

        sessionOrderRepository.delete(order);
        log.info("Deleted session order: orderId={}, sessionId={}, shopId={}", orderId, sessionId, shopId);
    }

    private TableSession getOwnedSession(UUID sessionId, UUID shopId) {
        return tableSessionRepository.findByIdAndShopId(sessionId, shopId)
                .orElseThrow(() -> new TableSessionNotFoundException(sessionId));
    }

    private TableSession getOwnedActiveOrAnySession(UUID sessionId, UUID shopId) {
        return getOwnedSession(sessionId, shopId);
    }

    private void requireActive(TableSession session) {
        if (session.getStatus() != TableSessionStatus.ACTIVE) {
            throw new TableSessionNotActiveException(
                    "Table session is not active: " + session.getId());
        }
    }
}
