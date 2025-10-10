package com.dineswift.restaurant_service.payload.request.orderItem;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
@Builder
public class CartAmountUpdateRequest {

    @NotNull(message = "Total Dish Price cannot be null")
    private BigDecimal totalDishPrice;

    @NotNull(message = "isRemoved flag cannot be null")
    private boolean isRemoved;
}
