package com.dineswift.restaurant_service.payload.request.tableBooking;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class AddOrderItemRequest {

    private UUID dishId;
    private Integer quantity;

}
