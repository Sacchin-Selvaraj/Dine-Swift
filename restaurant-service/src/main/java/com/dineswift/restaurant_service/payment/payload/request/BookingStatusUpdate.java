package com.dineswift.restaurant_service.payment.payload.request;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class BookingStatusUpdate {
    private UUID tableBookingId;
    private String bookingStatus;
}
