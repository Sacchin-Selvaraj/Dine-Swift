package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.model.response.booking.TableBookingDto;
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
    public ResponseEntity<TableBookingDto> bookTable(@PathVariable UUID cartId,@Valid @RequestBody BookingRequest bookingRequest){
        log.info("Received booking request for cartId: {}", cartId);
        TableBookingDto tableBookingDto = bookingService.bookTable(cartId, bookingRequest);
        log.info("Booking successful for cartId: {}", cartId);
        return ResponseEntity.ok(tableBookingDto);
    }

    @PostMapping("/pay-now/{tableBookingId}")
    public ResponseEntity<PaymentCreateResponse> payNow(@PathVariable UUID tableBookingId) {
        log.info("Received pay-now request for tableBookingId: {}", tableBookingId);
        PaymentCreateResponse response = bookingService.getPaymentCreateResponse(tableBookingId);
        log.info("Pay-now link generated for tableBookingId: {}", tableBookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay-bill/{bookingId}")
    public ResponseEntity<PaymentCreateResponse> payBill(@PathVariable UUID bookingId) {
        log.info("Received pay-now request for tableBookingId: {}", bookingId);
        PaymentCreateResponse response = bookingService.generateBill(bookingId);
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
