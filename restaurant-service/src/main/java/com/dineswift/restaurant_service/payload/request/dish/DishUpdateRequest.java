package com.dineswift.restaurant_service.payload.request.dish;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class DishUpdateRequest {

    private String dishName;
    private BigDecimal dishPrice;
    private String dishDescription;
    private Boolean isAvailable;
    private BigDecimal discount;
    private String dishComments;
    private Boolean isVeg;
    private Boolean isActive;
}
