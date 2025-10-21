package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.entites.BookingStatus;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class PaymentCreateResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentName;
    private String description;
    private String email;
    private UUID tableBookingId;
    private BookingStatus bookingStatus;
}
