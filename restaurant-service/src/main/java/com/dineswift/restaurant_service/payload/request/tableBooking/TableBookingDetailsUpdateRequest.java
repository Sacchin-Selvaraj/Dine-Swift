package com.dineswift.restaurant_service.payload.request.tableBooking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Data
@RequiredArgsConstructor
public class TableBookingDetailsUpdateRequest {

    private Boolean isPendingAmountPaid;
    private Boolean isUpfrontPaid;
    private LocalTime actualDineInTime;
    private LocalTime actualDineOutTime;

}
