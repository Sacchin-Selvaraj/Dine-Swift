package com.dineswift.restaurant_service.payment.payload.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PaymentRefundDto {

    private UUID refundId;
    private String razorpayRefundId;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String reason;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
