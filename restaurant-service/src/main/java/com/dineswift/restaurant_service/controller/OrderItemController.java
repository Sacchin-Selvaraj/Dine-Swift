package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/restaurant/order-items")
@Slf4j
public class OrderItemController {

    private final OrderService orderService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PostMapping("/add-item/{cartId}/{dishId}/{quantity}")
    public ResponseEntity<Void> addItemToOrderItem(@PathVariable UUID cartId,
                                              @PathVariable UUID dishId,
                                              @PathVariable Integer quantity) {
        log.info("Adding item to cart: cartId={}, dishId={}, quantity={}", cartId, dishId, quantity);
        orderService.addItemToOrderItem(cartId, dishId, quantity);
        return ResponseEntity.created(null).build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @PatchMapping("/update-item/{orderItemId}/{quantity}")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable UUID orderItemId,
                                                   @PathVariable Integer quantity) {
        log.info("Updating item quantity: orderItemId={}, quantity={}", orderItemId, quantity);
        orderService.updateItemQuantity(orderItemId, quantity);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @DeleteMapping("/delete-item/{orderItemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID orderItemId) {
        log.info("Deleting item: orderItemId={}", orderItemId);
        orderService.deleteItem(orderItemId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/get-order-items/{cartId}")
    public ResponseEntity<List<OrderItemDto>> getOrderItemsByCartId(@PathVariable UUID cartId) {
        log.info("Fetching order items for cartId={}", cartId);
        List<OrderItemDto> orderItems = orderService.getOrderItemsByCartId(cartId);
        return ResponseEntity.ok(orderItems);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_USER')")
    @GetMapping("/get-order-items-booking/{tableBookingId}")
    public ResponseEntity<Page<OrderItemDto>> getOrderItemsByTableBookingId(
            @PathVariable UUID tableBookingId,
            @RequestParam(value = "page") Integer pageNo,
            @RequestParam(value = "size") Integer pageSize
    ) {
        log.info("Fetching order items for tableBookingId={}", tableBookingId);
        Page<OrderItemDto> orderItems = orderService.getOrderItemsByTableBookingId(tableBookingId, pageNo, pageSize);
        return ResponseEntity.ok(orderItems);
    }
}
