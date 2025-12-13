package com.dineswift.restaurant_service.payment.service;

import com.dineswift.restaurant_service.exception.PaymentException;
import com.dineswift.restaurant_service.mapper.PaymentMapper;
import com.dineswift.restaurant_service.model.PaymentRefund;
import com.dineswift.restaurant_service.payment.payload.response.PaymentRefundDto;
import com.dineswift.restaurant_service.payment.repository.PaymentRefundRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RefundService {

    private final PaymentRefundRepository paymentRefundRepository;
    private final PaymentMapper paymentMapper;

    public List<PaymentRefundDto> getRefundDetailsByTableBookingId(UUID tableBookingId) {
        log.info("Retrieving refund details for tableBookingId={}", tableBookingId);
        List<PaymentRefund> refundDetails = paymentRefundRepository.findAllByTableBookingId(tableBookingId);

        if (refundDetails.isEmpty()){
            log.warn("No refund records found for tableBookingId={}", tableBookingId);
            throw new PaymentException("No refund records found for the provided table booking ID.");
        }

        log.info("Found {} refund records for tableBookingId={}", refundDetails.size(), tableBookingId);
        return refundDetails.stream().map(paymentMapper::toDto).toList();
    }
}
