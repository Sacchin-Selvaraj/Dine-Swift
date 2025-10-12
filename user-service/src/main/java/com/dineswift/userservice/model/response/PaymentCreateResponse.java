package com.dineswift.userservice.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PaymentCreateResponse {

    private String orderId;
    private String amount;
    private String currency;
    private String paymentName;
    private String description;
    private String email;
}
