package com.dineswift.restaurant_service.payload.response.tableBooking;

import com.dineswift.restaurant_service.model.BookingStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCreateResponse {

    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentName;
    private String description;
    private UUID tableBookingId;
    private BookingStatus bookingStatus;

}
