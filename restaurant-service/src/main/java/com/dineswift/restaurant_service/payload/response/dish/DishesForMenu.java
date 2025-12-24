package com.dineswift.restaurant_service.payload.response.dish;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class DishesForMenu {
    private UUID dishId;
    private String dishName;
    private BigDecimal dishPrice;

    public DishesForMenu(UUID dishId, String dishName, BigDecimal dishPrice) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.dishPrice = dishPrice;
    }
}
