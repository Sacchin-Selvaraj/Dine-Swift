package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.tableBooking.AddOrderItemRequest;
import com.dineswift.restaurant_service.payload.request.tableBooking.BookingRequest;
import com.dineswift.restaurant_service.payload.request.tableBooking.CancellationDetails;
import com.dineswift.restaurant_service.payload.request.tableBooking.QuantityUpdateRequest;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.PaymentCreateResponse;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import com.dineswift.restaurant_service.service.TableBookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/restaurant/table-booking")
public class TableBookingController {

    private final TableBookingService tableBookingService;

    @PreAuthorize(("hasAnyRole('ROLE_USER')"))
    @PostMapping("/create-order/{cartId}")
    public ResponseEntity<TableBookingDto> bookTable(@PathVariable UUID cartId, @RequestBody BookingRequest bookingRequest){
        TableBookingDto bookingDto = tableBookingService.createOrder(cartId, bookingRequest);
        log.info("Order created successfully for cartId: {}", cartId);
        return ResponseEntity.ok(bookingDto);
    }
    @PreAuthorize(("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')"))
    @DeleteMapping("/cancel-booking/{tableBookingId}")
    public ResponseEntity<MessageResponse> cancelBooking(@PathVariable UUID tableBookingId, @RequestBody CancellationDetails cancellationDetails){
        String response = tableBookingService.cancelBooking(tableBookingId, cancellationDetails);
        log.info("Booking cancelled successfully for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PreAuthorize(("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER','ROLE_CHEF', 'ROLE_WAITER')"))
    @GetMapping("/view-booking/{tableBookingId}")
    public ResponseEntity<TableBookingDto> viewBooking(@PathVariable UUID tableBookingId) {
        TableBookingDto bookingDetails = tableBookingService.viewBooking(tableBookingId);
        log.info("Fetched booking details for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(bookingDetails);
    }
    @PreAuthorize(("hasAnyRole('ROLE_USER')"))
    @PatchMapping("/change-order-item/{orderItemsId}")
    public ResponseEntity<OrderItemDto> updateOrderItem(@PathVariable UUID orderItemsId, @RequestBody QuantityUpdateRequest quantityUpdateRequest) {
        OrderItemDto updatedItem = tableBookingService.updateOrderItem(orderItemsId, quantityUpdateRequest);
        log.info("Updated order item successfully for orderItemId: {}", orderItemsId);
        return ResponseEntity.ok(updatedItem);
    }
    @PreAuthorize(("hasAnyRole('ROLE_USER')"))
    @DeleteMapping("/remove-order-item/{orderItemsId}")
    public ResponseEntity<Void> removeOrderItem(@PathVariable UUID orderItemsId) {
        tableBookingService.removeOrderItem(orderItemsId);
        log.info("Removed order item successfully for orderItemId: {}", orderItemsId);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize(("hasAnyRole('ROLE_USER')"))
    @PostMapping("/add-order-item/{tableBookingId}")
    public ResponseEntity<OrderItemDto> addOrderItem(@PathVariable UUID tableBookingId, @RequestBody AddOrderItemRequest addOrderItemRequest) {
        OrderItemDto addedItem = tableBookingService.addOrderItem(tableBookingId, addOrderItemRequest);
        log.info("Added order item successfully for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(addedItem);
    }

}
