package com.dineswift.restaurant_service.payload.request.tableBooking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CancellationDetails {

    private String cancellationReason;
}
