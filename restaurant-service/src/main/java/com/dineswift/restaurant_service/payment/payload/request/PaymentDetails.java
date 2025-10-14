package com.dineswift.restaurant_service.payment.payload.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PaymentDetails {

    private String paymentId;
    private String orderId;
    private String signature;
}
