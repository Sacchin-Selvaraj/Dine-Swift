package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.request.BookingRequest;
import com.dineswift.userservice.model.response.PaymentCreateResponse;
import com.dineswift.userservice.model.response.booking.BookingStatusUpdate;
import com.dineswift.userservice.model.response.booking.TableBookingDto;
import com.dineswift.userservice.model.response.booking.TableBookingResponse;
import com.dineswift.userservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/user/booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/book-table/{cartId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TableBookingResponse> bookTable(@PathVariable UUID cartId, @Valid @RequestBody BookingRequest bookingRequest){
        log.info("Received booking request for cartId: {}", cartId);
        TableBookingResponse tableBookingResponse = bookingService.bookTable(cartId, bookingRequest);
        log.info("Booking successful for cartId: {}", cartId);
        return ResponseEntity.ok(tableBookingResponse);
    }

    @PostMapping("/pay-now/{tableBookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<PaymentCreateResponse> payNow(@PathVariable UUID tableBookingId) {
        log.info("Received pay-now request for tableBookingId: {}", tableBookingId);
        PaymentCreateResponse response = bookingService.getPaymentCreateResponse(tableBookingId);
        log.info("Pay-now link generated for tableBookingId: {}", tableBookingId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay-bill/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<PaymentCreateResponse> payBill(@PathVariable UUID bookingId) {
        log.info("Received pay-now request for bookingId: {}", bookingId);
        PaymentCreateResponse response = bookingService.generateBill(bookingId);
        log.info("Pay-now link generated for bookingId: {}", bookingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/view-table-booking/{bookingId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<TableBookingDto> viewTableBooking(@PathVariable UUID bookingId) {
        log.info("Received view booking request for tableBookingId: {}", bookingId);
        TableBookingDto bookedTableDetails = bookingService.viewTableBooking(bookingId);
        log.info("Fetched booking details for tableBookingId: {}", bookingId);
        return ResponseEntity.ok(bookedTableDetails);
    }

    @PatchMapping("/update-booking-status")
    public ResponseEntity<Void> updateBookingStatus(@RequestBody BookingStatusUpdate statusUpdate){
        log.info("Received booking status update for bookingId: {}", statusUpdate.getTableBookingId());
        bookingService.updateBookingStatus(statusUpdate.getTableBookingId(),statusUpdate.getBookingStatus());
        log.info("Updated booking status for bookingId: {}", statusUpdate.getTableBookingId());
        return ResponseEntity.ok().build();
    }

}
