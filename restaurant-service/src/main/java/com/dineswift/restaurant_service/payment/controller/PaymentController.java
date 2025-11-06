package com.dineswift.restaurant_service.payment.controller;

import com.dineswift.restaurant_service.payload.response.tableBooking.PaymentCreateResponse;
import com.dineswift.restaurant_service.payment.payload.request.PaymentDetails;
import com.dineswift.restaurant_service.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentDetails paymentDetails){
        boolean isValid = paymentService.verifyPayment(paymentDetails);
        if(isValid){
            log.info("Payment verified successfully for paymentId: {}", paymentDetails.getPaymentId());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } else {
            log.warn("Payment verification failed for paymentId: {}", paymentDetails.getPaymentId());
           return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment verification failed");
        }
    }
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/pay-now/{tableBookingId}")
    public ResponseEntity<PaymentCreateResponse> payBill(@PathVariable UUID tableBookingId) {
        PaymentCreateResponse paymentCreateDetails = paymentService.generatePayNow(tableBookingId);
        log.info("Generated pay-now link for bookingId: {}", tableBookingId);
        return ResponseEntity.ok(paymentCreateDetails);
    }

}
