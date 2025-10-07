package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.DishException;
import com.dineswift.restaurant_service.exception.OrderItemException;
import com.dineswift.restaurant_service.mapper.OrderItemMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
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

    public void addItemToCart(UUID cartId, UUID dishId, Integer quantity) {
        log.info("Checking quantity: {}", quantity);
        checkQuantity(quantity);
        checkCartIdIsValid(cartId);

        Dish dish = dishRepository.findByIdAndIsActive(dishId).orElseThrow(() -> new DishException("Dish not found or inactive"));
        log.info("Dish found: {}", dish.getDishName());

        OrderItem existingOrderItem = orderItemRepository.findByCartIdAndDish(cartId, dish).orElse(null);
        if (existingOrderItem != null) {
            log.info("Existing OrderItem found, updating quantity: {}", existingOrderItem);
            int newQuantity = existingOrderItem.getQuantity() + quantity;
            checkQuantity(newQuantity);
            existingOrderItem.setQuantity(newQuantity);
            existingOrderItem.setTotalPrice(existingOrderItem.getPrice().multiply(BigDecimal.valueOf(existingOrderItem.getQuantity())));
            orderItemRepository.save(existingOrderItem);
            return;
        }

        OrderItem updatedOrderItem = orderItemMapper.toEntity(cartId, dish, quantity);
        log.info("OrderItem created: {}", updatedOrderItem);
        orderItemRepository.save(updatedOrderItem);
    }

    private void checkCartIdIsValid(UUID cartId) {
        log.info("Check whether the CartId is present in User Service or not: {}", cartId);
        RestClient restClient = new RestClient();
    }

    public void updateItemQuantity(UUID orderItemId, Integer quantity) {

        checkQuantity(quantity);
        OrderItem orderItem = orderItemRepository.findById(orderItemId).orElseThrow(() -> new OrderItemException("Order item not found"));
        log.info("OrderItem found: {}", orderItem);
        orderItem.setQuantity(quantity);
        orderItem.setTotalPrice(orderItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
        orderItemRepository.save(orderItem);

    }

    public static void checkQuantity(Integer quantity) {
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
}
