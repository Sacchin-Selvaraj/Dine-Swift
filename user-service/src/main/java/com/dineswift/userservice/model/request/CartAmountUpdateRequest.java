package com.dineswift.userservice.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class CartAmountUpdateRequest {

    @NotNull(message = "Total Dish Price cannot be null")
    private BigDecimal totalDishPrice;

    @NotNull(message = "isRemoved flag cannot be null")
    private boolean isRemoved;
}
