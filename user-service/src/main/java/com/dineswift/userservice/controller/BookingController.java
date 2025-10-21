package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.model.response.TableBookingDto;
import com.dineswift.userservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book-table/{cartId}")
    public ResponseEntity<PaymentCreateResponse> bookTable(@PathVariable UUID cartId,@Valid @RequestBody BookingRequest bookingRequest){
        log.info("Received booking request for cartId: {}", cartId);
        PaymentCreateResponse response = bookingService.bookTable(cartId, bookingRequest);
        log.info("Booking successful for cartId: {}", cartId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay-now/{bookingId}")
    public ResponseEntity<PaymentCreateResponse> payNow(@PathVariable UUID bookingId) {
        log.info("Received pay-now request for tableBookingId: {}", bookingId);
        PaymentCreateResponse response = bookingService.generatePayNow(bookingId);
        log.info("Pay-now link generated for tableBookingId: {}", bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view-table-booking/{bookingId}")
    public ResponseEntity<TableBookingDto> viewTableBooking(@PathVariable UUID bookingId) {
        log.info("Received view booking request for tableBookingId: {}", bookingId);
        TableBookingDto bookedTableDetails = bookingService.viewTableBooking(bookingId);
        log.info("Fetched booking details for tableBookingId: {}", bookingId);
        return ResponseEntity.ok(bookedTableDetails);
    }

}
