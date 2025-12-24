package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.tableBooking.*;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDto;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingDtoWoRestaurant;
import com.dineswift.restaurant_service.payload.response.tableBooking.TableBookingResponse;
import com.dineswift.restaurant_service.service.CustomPageDto;
import com.dineswift.restaurant_service.service.TableBookingService;
import com.dineswift.restaurant_service.records.TableBookingFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/restaurant/table-booking")
public class TableBookingController {

    private final TableBookingService tableBookingService;

    @PreAuthorize(("hasAnyRole('ROLE_USER')"))
    @PostMapping("/create-order/{cartId}")
    public ResponseEntity<TableBookingResponse> bookTable(@PathVariable UUID cartId,
                                                          @RequestBody BookingRequest bookingRequest){

        TableBookingResponse bookingDto = tableBookingService.createOrder(cartId, bookingRequest);
        log.info("Order created successfully for cartId: {}", cartId);
        return ResponseEntity.ok(bookingDto);
    }
    @PreAuthorize(("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_MANAGER')"))
    @DeleteMapping("/cancel-booking/{tableBookingId}")
    public ResponseEntity<MessageResponse> cancelBooking(@PathVariable UUID tableBookingId,
                                                         @RequestBody CancellationDetails cancellationDetails){

        String response = tableBookingService.cancelBooking(tableBookingId, cancellationDetails);
        log.info("Booking cancelled successfully for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(new MessageResponse(response));
    }

    @PreAuthorize(("hasAnyRole('USER', 'ROLE_ADMIN', 'ROLE_MANAGER','ROLE_CHEF', 'ROLE_WAITER')"))
    @GetMapping("/view-booking/{tableBookingId}")
    public ResponseEntity<TableBookingDto> viewBooking(@PathVariable UUID tableBookingId) {
        TableBookingDto bookingDetails = tableBookingService.viewBooking(tableBookingId);
        log.info("Fetched booking details for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(bookingDetails);
    }
    @PreAuthorize(("hasAnyRole('ROLE_USER')"))
    @PatchMapping("/change-order-item/{orderItemsId}")
    public ResponseEntity<Void> updateOrderItem(@PathVariable UUID orderItemsId,
                                                           @RequestBody QuantityUpdateRequest quantityUpdateRequest) {

        tableBookingService.updateOrderItem(orderItemsId, quantityUpdateRequest);
        log.info("Updated order item successfully for orderItemId: {}", orderItemsId);

        return ResponseEntity.ok().build();
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
    public ResponseEntity<Void> addOrderItem(@PathVariable UUID tableBookingId,
                                                        @RequestBody AddOrderItemRequest addOrderItemRequest) {

        tableBookingService.addOrderItem(tableBookingId, addOrderItemRequest);
        log.info("Added order item successfully for bookingId: {}", tableBookingId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("(hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER','ROLE_CHEF', 'ROLE_WAITER'))")
    @GetMapping("/get-table-booking-details/{restaurantId}")
    public ResponseEntity<CustomPageDto<TableBookingDtoWoRestaurant>> getTableBookingDetails(
            @PathVariable UUID restaurantId,
            @RequestParam(value = "page") Integer pageNo,
            @RequestParam(value = "size") Integer pageSize,
            @RequestParam(value = "tableNumber", required = false) String tableNumber,
            @RequestParam(value = "bookingDate", required = false) LocalDate bookingDate,
            @RequestParam(value = "dineInTime", required = false) LocalTime dineInTime,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "noOfGuest", required = false) Integer noOfGuest,
            @RequestParam(value = "bookingStatus", required = false) String bookingStatus,
            @RequestParam(value = "dishStatus", required = false) String dishStatus,
            @RequestParam(value = "sortBy",defaultValue = "bookingDate", required = false) String sortBy,
            @RequestParam(value = "sortDir",defaultValue = "asc", required = false) String sortDir
    ) {
        TableBookingFilter filter=new TableBookingFilter(
                restaurantId,
                pageNo,
                pageSize,
                tableNumber,
                bookingDate,
                dineInTime,
                duration,
                noOfGuest,
                bookingStatus,
                dishStatus,
                sortBy,
                sortDir
        );
        CustomPageDto<TableBookingDtoWoRestaurant> bookings = tableBookingService.getTableBookingDetails(filter);
        log.info("Fetched table booking details for restaurantId: {}", restaurantId);
        return ResponseEntity.ok(bookings);
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER','ROLE_CHEF', 'ROLE_WAITER')"))
    @PutMapping("/update-status/{tableBookingId}")
    public ResponseEntity<Void> updateBookingStatus(@PathVariable UUID tableBookingId,
                                                               @RequestBody TableBookingStatusUpdateRequest statusUpdateRequest) {

        tableBookingService.updateBookingStatus(tableBookingId, statusUpdateRequest);
        log.info("Updated booking status successfully for bookingId: {}", tableBookingId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize(("hasAnyRole('ROLE_ADMIN', 'ROLE_MANAGER', 'ROLE_WAITER','ROLE_STAFF')"))
    @PutMapping("/update-details/{tableBookingId}")
    public ResponseEntity<Void> updateBookingDetails(@PathVariable UUID tableBookingId,
                                                                @RequestBody TableBookingDetailsUpdateRequest detailsUpdateRequest) {

        tableBookingService.updateBookingDetails(tableBookingId, detailsUpdateRequest);
        log.info("Updated booking details successfully for bookingId: {}", tableBookingId);

        return ResponseEntity.ok().build();
    }

}
