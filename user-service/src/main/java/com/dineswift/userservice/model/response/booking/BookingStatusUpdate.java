package com.dineswift.userservice.model.response.booking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class BookingStatusUpdate {
    private UUID tableBookingId;
    private String bookingStatus;
}
