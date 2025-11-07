package com.dineswift.restaurant_service.payload.request.tableBooking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class TableBookingStatusUpdateRequest {

    private String bookingStatus;
    private String dishStatus;

}
