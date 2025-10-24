package com.dineswift.restaurant_service.payload.request.tableBooking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class QuantityUpdateRequest {

    @NotNull(message = "New quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private int newQuantity;
}
