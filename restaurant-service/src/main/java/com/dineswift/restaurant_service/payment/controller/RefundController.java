package com.dineswift.restaurant_service.payment.controller;

import com.dineswift.restaurant_service.model.PaymentRefund;
import com.dineswift.restaurant_service.payment.payload.response.PaymentRefundDto;
import com.dineswift.restaurant_service.payment.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/restaurant/refund")
public class RefundController {

    private final RefundService refundService;

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/details/{tableBookingId}")
    public ResponseEntity<List<PaymentRefundDto>> getRefundDetails(@PathVariable UUID tableBookingId) {
        log.info("Fetching refund details for tableBookingId={}", tableBookingId);
        List<PaymentRefundDto> refundDetails = refundService.getRefundDetailsByTableBookingId(tableBookingId);
        return ResponseEntity.ok(refundDetails);
    }
}
