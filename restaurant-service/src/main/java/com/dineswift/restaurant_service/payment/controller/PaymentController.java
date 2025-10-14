package com.dineswift.restaurant_service.payment.controller;

import com.dineswift.restaurant_service.payment.payload.request.PaymentDetails;
import com.dineswift.restaurant_service.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

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

}
