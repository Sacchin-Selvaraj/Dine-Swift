package com.dineswift.restaurant_service.controller;


import com.dineswift.restaurant_service.payload.request.tableBooking.BookingRequest;
import com.dineswift.restaurant_service.payload.response.tableBooking.PaymentCreateResponse;
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
    public ResponseEntity<PaymentCreateResponse> bookTable(@PathVariable UUID cartId, @RequestBody BookingRequest bookingRequest){
        PaymentCreateResponse paymentCreateResponse = tableBookingService.createOrder(cartId, bookingRequest);
        log.info("Order created successfully for cartId: {}", cartId);
        return ResponseEntity.ok(paymentCreateResponse);
    }
}
