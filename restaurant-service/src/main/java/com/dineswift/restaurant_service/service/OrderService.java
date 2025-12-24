package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.DishException;
import com.dineswift.restaurant_service.exception.OrderItemException;
import com.dineswift.restaurant_service.mapper.OrderItemMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import com.dineswift.restaurant_service.payload.request.orderItem.AddOrderItem;
import com.dineswift.restaurant_service.payload.request.orderItem.CartAmountUpdateRequest;
import com.dineswift.restaurant_service.payload.response.orderItem.CustomOrderItem;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;
    private final OrderItemMapper orderItemMapper;
    private final RestClient restClient;
    private final CacheManager cacheManager;

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:order-items-by-booking",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "userService:cartDto",
                            key = "@authService.getAuthenticatedId()"
                    )
            }
    )
    @Transactional
    public void addItemToOrderItem(AddOrderItem addOrderItemRequest) {

        UUID dishId = addOrderItemRequest.getDishId();
        Integer quantity = addOrderItemRequest.getQuantity();
        log.info("Checking quantity: {}", quantity);
        checkQuantity(quantity);

        UUID cartId = getCartIdFromUserService();

        Dish dish = dishRepository.findByIdAndIsActiveWithRestaurant(dishId)
                .orElseThrow(() -> new DishException("Dish not found or inactive"));

        log.info("Dish found: {}", dish.getDishName());
        CartAmountUpdateRequest amountUpdateRequest = getCartAmountUpdateRequest(quantity, dish);

        log.info("API Call to User-Service to update the cart total amount");
        updateCartTotalAmount(cartId, amountUpdateRequest);

        OrderItem existingOrderItem = orderItemRepository.findByCartIdAndDish(cartId, dish).orElse(null);
        if (existingOrderItem != null) {
            log.info("Existing OrderItem found, updating quantity: {}", addOrderItemRequest.getQuantity());
            int newQuantity = existingOrderItem.getQuantity() + quantity;

            checkQuantity(newQuantity);
            existingOrderItem.setQuantity(newQuantity);

            orderItemRepository.save(existingOrderItem);

            evictOrderItemCaches(cartId);
            return;
        }

        OrderItem updatedOrderItem = orderItemMapper.toEntity(cartId, dish, quantity);
        log.info("OrderItem created: {}", updatedOrderItem);
        orderItemRepository.save(updatedOrderItem);

        evictOrderItemCaches(cartId);
    }

    @Cacheable(
            value = "restaurant:cart-id-from-user-service",
            key = "@authService.getAuthenticatedId()",
            unless = "#result == null"
    )
    private UUID getCartIdFromUserService() {
        log.info("Fetching cartId from User-Service");
        ResponseEntity<UUID> response = restClient.get()
                .uri("/cart/current-cart-id")
                .retrieve()
                .toEntity(UUID.class);
        UUID cartId = response.getBody();
        if (cartId == null) {
            log.error("Failed to retrieve cartId from User-Service");
            throw new OrderItemException("Failed to retrieve cart ID from User Service");
        }
        log.info("Retrieved cartId: {}", cartId);
        return cartId;
    }

    private static CartAmountUpdateRequest getCartAmountUpdateRequest(Integer quantity, Dish dish) {
        log.info("Calculating the total price and sending to user service to update the cart total amount");

        CartAmountUpdateRequest amountUpdateRequest = new CartAmountUpdateRequest();
        amountUpdateRequest.setRemoved(false);
        BigDecimal finalDishPrice = dish.getDishPrice().subtract(
                dish.getDishPrice()
                        .multiply(dish.getDiscount())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        log.info("Final dish price after discount: {}", finalDishPrice);

        finalDishPrice = finalDishPrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalTotalPrice = finalDishPrice.multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        amountUpdateRequest.setTotalDishPrice(finalTotalPrice);
        return amountUpdateRequest;
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:order-items-by-booking",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "userService:cartDto",
                            key = "@authService.getAuthenticatedId()"
                    )
            }
    )
    @Transactional
    public void updateItemQuantity(UUID orderItemId, Integer quantity) {

        checkQuantity(quantity);
        OrderItem orderItem = orderItemRepository.findByIdAndDish(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order item not found"));

        log.info("OrderItem found: {}", orderItem);
        CartAmountUpdateRequest amountUpdateRequest = getAmountUpdateRequest(quantity, orderItem);

        log.info("Amount update request prepared: {} will be sent to User-Service", amountUpdateRequest);
        updateCartTotalAmount(orderItem.getCartId(), amountUpdateRequest);

        orderItem.setQuantity(quantity);
        orderItemRepository.save(orderItem);

        log.info("OrderItem quantity updated: {}", orderItem);
        evictOrderItemCaches(orderItem.getCartId());
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:order-items-by-booking",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = "userService:cartDto",
                            key = "@authService.getAuthenticatedId()"
                    )
            }
    )
    @Transactional
    public void deleteItem(UUID orderItemId) {

        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new OrderItemException("Order item not found"));

        log.info("OrderItem found for deletion: {}", orderItem);
        orderItemRepository.delete(orderItem);

        evictOrderItemCaches(orderItem.getCartId());
    }

    @Cacheable(
            value = "restaurant:order-items-by-cart",
            key = "#cartId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public CustomOrderItem getOrderItemsByCartId(UUID cartId) {

        List<OrderItem> orderItems = orderItemRepository.findAllByCartId(cartId);

        if (orderItems.isEmpty()){
            log.warn("No order items found for cartId: {}", cartId);
            return new CustomOrderItem(List.of());
        }

        List<OrderItemDto> orderItemDtos = orderItemMapper.toListDto(orderItems);

        log.info("Order items fetched for cartId {}", cartId);
        return new CustomOrderItem(orderItemDtos);
    }

    private static void checkQuantity(Integer quantity) {
        final int QUANTITY_LIMIT = 20;
        final int QUANTITY_MIN = 1;
        if (quantity > QUANTITY_LIMIT){
            log.error("Quantity {} exceeds the limit of {}", quantity, QUANTITY_LIMIT);
            throw new OrderItemException("Quantity exceeds the limit of " + QUANTITY_LIMIT);
        }
        if (quantity<QUANTITY_MIN){
            log.error("Quantity {} is below the minimum of {}", quantity, QUANTITY_MIN);
            throw new OrderItemException("Quantity must be at least " + QUANTITY_MIN);
        }
    }


    private void updateCartTotalAmount(UUID cartId, CartAmountUpdateRequest cartAmountUpdateRequest) {

        log.info("Updating cart total amount: cartId={}, totalAmount={}", cartId, cartAmountUpdateRequest);
        ResponseEntity<Void> response = restClient.patch()
                .uri("/cart/update-cart-amount/{cartId}",cartId)
                .body(cartAmountUpdateRequest)
                .header("Content-Type", "application/json")
                .retrieve()
                .toBodilessEntity();

        log.info("Cart total amount updated successfully for cartId={}", cartId);
    }

    private static CartAmountUpdateRequest getAmountUpdateRequest(Integer quantity, OrderItem orderItem) {
        CartAmountUpdateRequest amountUpdateRequest = new CartAmountUpdateRequest();
        BigDecimal amountDifference;
        int quantityDifference = quantity - orderItem.getQuantity();

        if (quantityDifference>=0) {
            amountUpdateRequest.setRemoved(false);
            amountDifference = orderItem.getPrice().multiply(BigDecimal.valueOf(quantityDifference));
        }
        else {
            amountUpdateRequest.setRemoved(true);
            amountDifference = orderItem.getPrice().multiply(BigDecimal.valueOf(Math.abs(quantityDifference)));
        }

        amountUpdateRequest.setTotalDishPrice(amountDifference);
        return amountUpdateRequest;
    }

    @Cacheable(
            value = "restaurant:order-items-by-booking",
            key = "#tableBookingId + '-' + #pageNo + '-' + #pageSize",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public CustomPageDto<OrderItemDto> getOrderItemsByTableBookingId(UUID tableBookingId,
                                                                     Integer pageNo, Integer pageSize) {

        log.info("Fetching order items for tableBookingId: {}", tableBookingId);
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<OrderItem> orderItemsPage = orderItemRepository.findAllByTableBookingId(tableBookingId, pageable);

        if (orderItemsPage.isEmpty()) {
            log.error("No order items found for tableBookingId: {}", tableBookingId);
            return new CustomPageDto<>(Page.empty());
        }

        Page<OrderItemDto> orderItemDtos = getOrderItemsDtoList(orderItemsPage);
        log.info("Order items fetched for tableBookingId {}: {}", tableBookingId, orderItemDtos);
        return new CustomPageDto<>(orderItemDtos);

    }

    @Transactional(readOnly = true)
    private Page<OrderItemDto> getOrderItemsDtoList(Page<OrderItem> orderItemsPage) {
        List<OrderItem> orderItemList = orderItemsPage.getContent();
        Map<UUID,OrderItemDto> orderItemDtoMap = orderItemMapper.toListDtoAfterBooking(orderItemList);
        log.info("Converted OrderItems to OrderItemDtos");
        return orderItemsPage.map(orderItem -> orderItemDtoMap.get(orderItem.getOrderItemsId()));
    }

    public void evictOrderItemCaches(UUID cartId) {
        log.info("Evicting caches related to order items for cartId: {}", cartId);
        Objects.requireNonNull(cacheManager.getCache("restaurant:order-items-by-cart")).evict(cartId);
    }
}
