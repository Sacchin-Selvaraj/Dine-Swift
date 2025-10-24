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
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/table-booking")
public class TableBookingController {

    private final TableBookingService tableBookingService;

    @PostMapping("/create-order/{cartId}")
    public ResponseEntity<TableBookingDto> bookTable(@PathVariable UUID cartId, @RequestBody BookingRequest bookingRequest){
        TableBookingDto bookingDto = tableBookingService.createOrder(cartId, bookingRequest);
        log.info("Order created successfully for cartId: {}", cartId);
        return ResponseEntity.ok(bookingDto);
    }

    @DeleteMapping("/cancel-booking/{tableBookingId}")
    public ResponseEntity<MessageResponse> cancelBooking(@PathVariable UUID tableBookingId, @RequestBody CancellationDetails cancellationDetails){
        String response = tableBookingService.cancelBooking(tableBookingId, cancellationDetails);
        log.info("Booking cancelled successfully for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @GetMapping("/view-booking/{tableBookingId}")
    public ResponseEntity<TableBookingDto> viewBooking(@PathVariable UUID tableBookingId) {
        TableBookingDto bookingDetails = tableBookingService.viewBooking(tableBookingId);
        log.info("Fetched booking details for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(bookingDetails);
    }

    @PatchMapping("/change-order-item/{orderItemsId}")
    public ResponseEntity<OrderItemDto> updateOrderItem(@PathVariable UUID orderItemsId, @RequestBody QuantityUpdateRequest quantityUpdateRequest) {
        OrderItemDto updatedItem = tableBookingService.updateOrderItem(orderItemsId, quantityUpdateRequest);
        log.info("Updated order item successfully for orderItemId: {}", orderItemsId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/remove-order-item/{orderItemsId}")
    public ResponseEntity<Void> removeOrderItem(@PathVariable UUID orderItemsId) {
        tableBookingService.removeOrderItem(orderItemsId);
        log.info("Removed order item successfully for orderItemId: {}", orderItemsId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/add-order-item/{tableBookingId}")
    public ResponseEntity<OrderItemDto> addOrderItem(@PathVariable UUID tableBookingId, @RequestBody AddOrderItemRequest addOrderItemRequest) {
        OrderItemDto addedItem = tableBookingService.addOrderItem(tableBookingId, addOrderItemRequest);
        log.info("Added order item successfully for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(addedItem);
    }

}
