package com.dineswift.restaurant_service.payment.payload.response;

import com.dineswift.restaurant_service.model.PaymentMethod;
import com.dineswift.restaurant_service.model.PaymentStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PaymentDto {

    private UUID paymentId;
    private String paymentName;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private ZonedDateTime paymentDate;
    private String transactionId;
    private String orderId;
    private ZonedDateTime createdAt;
    private String failureReason;

}
