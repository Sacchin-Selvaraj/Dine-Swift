package com.dineswift.restaurant_service.payload.request.orderItem;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class AddOrderItem {

    @NotNull(message = "Dish ID cannot be null")
    private UUID dishId;

    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;
}
