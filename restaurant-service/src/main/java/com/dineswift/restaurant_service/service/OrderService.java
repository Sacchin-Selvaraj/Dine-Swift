package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.DishException;
import com.dineswift.restaurant_service.exception.OrderItemException;
import com.dineswift.restaurant_service.mapper.OrderItemMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import com.dineswift.restaurant_service.payload.request.orderItem.CartAmountUpdateRequest;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderItemRepository orderItemRepository;
    private final DishRepository dishRepository;
    private final OrderItemMapper orderItemMapper;
    private final RestClient restClient;

    public void addItemToOrderItem(UUID cartId, UUID dishId, Integer quantity) {
        log.info("Checking quantity: {}", quantity);
        checkQuantity(quantity);
        checkCartIdIsValid(cartId);

        Dish dish = dishRepository.findByIdAndIsActive(dishId).orElseThrow(() -> new DishException("Dish not found or inactive"));
        log.info("Dish found: {}", dish.getDishName());
        CartAmountUpdateRequest amountUpdateRequest = getCartAmountUpdateRequest(quantity, dish);

        log.info("API Call to User-Service to update the cart total amount");
        updateCartTotalAmount(cartId, amountUpdateRequest);

        OrderItem existingOrderItem = orderItemRepository.findByCartIdAndDish(cartId, dish).orElse(null);
        if (existingOrderItem != null) {
            log.info("Existing OrderItem found, updating quantity: {}", existingOrderItem);
            int newQuantity = existingOrderItem.getQuantity() + quantity;
            checkQuantity(newQuantity);
            existingOrderItem.setQuantity(newQuantity);
            orderItemRepository.save(existingOrderItem);
            return;
        }

        OrderItem updatedOrderItem = orderItemMapper.toEntity(cartId, dish, quantity);
        log.info("OrderItem created: {}", updatedOrderItem);
        orderItemRepository.save(updatedOrderItem);
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


    public void updateItemQuantity(UUID orderItemId, Integer quantity) {

        checkQuantity(quantity);
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(() -> new OrderItemException("Order item not found"));
        log.info("OrderItem found: {}", orderItem);
        CartAmountUpdateRequest amountUpdateRequest = getAmountUpdateRequest(quantity, orderItem);
        log.info("Amount update request prepared: {} will be sent to User-Service", amountUpdateRequest);
        updateCartTotalAmount(orderItem.getCartId(), amountUpdateRequest);

        orderItem.setQuantity(quantity);
        orderItemRepository.save(orderItem);

    }

    public void deleteItem(UUID orderItemId) {

        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(
                () -> new OrderItemException("Order item not found"));
        log.info("OrderItem found for deletion: {}", orderItem);
        orderItemRepository.delete(orderItem);
    }

    public List<OrderItemDto> getOrderItemsByCartId(UUID cartId) {

        List<OrderItem> orderItems = orderItemRepository.findAllByCartId(cartId);
        if (orderItems.isEmpty()){
            log.error("No order items found for cartId: {}", cartId);
            throw new OrderItemException("No order items found for the given cart ID");
        }

        List<OrderItemDto> orderItemDtos = orderItems.stream().map(orderItemMapper::toDto).toList();
        log.info("Order items fetched for cartId {}: {}", cartId, orderItemDtos);
        return orderItemDtos;
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

    private void checkCartIdIsValid(UUID cartId) {
        log.info("Check whether the CartId is present in User Service or not: {}", cartId);
        ResponseEntity<Boolean> response = restClient.get()
                .uri("cart/valid-cartId/{cartId}", cartId)
                .retrieve().toEntity(Boolean.class);
        if (response.getBody()==null || !response.getBody()) {
            log.error("Invalid cartId: {}", cartId);
            throw new OrderItemException("Invalid cart ID provided");
        }
        log.info("Valid cartId: {}", cartId);
    }

    private void updateCartTotalAmount(UUID cartId, CartAmountUpdateRequest cartAmountUpdateRequest) {
        log.info("Updating cart total amount: cartId={}, totalAmount={}", cartId, cartAmountUpdateRequest);
        ResponseEntity<Void> response = restClient.patch()
                .uri("cart/update-cart-amount/{cartId}",cartId)
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

    public Page<OrderItemDto> getOrderItemsByTableBookingId(UUID tableBookingId, Integer pageNo, Integer pageSize) {
        log.info("Fetching order items for tableBookingId: {}", tableBookingId);
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<OrderItem> orderItemsPage = orderItemRepository.findAllByTableBookingId(tableBookingId, pageable);
        if (orderItemsPage.isEmpty()) {
            log.error("No order items found for tableBookingId: {}", tableBookingId);
            throw new OrderItemException("No order items found for the given table booking ID");
        }
        Page<OrderItemDto> orderItemDtos = orderItemsPage.map(orderItemMapper::toDtoAfterBooking);
        log.info("Order items fetched for tableBookingId {}: {}", tableBookingId, orderItemDtos);
        return orderItemDtos;

    }
}
