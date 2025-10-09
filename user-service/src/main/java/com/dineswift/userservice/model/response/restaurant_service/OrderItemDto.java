package com.dineswift.userservice.model.response.restaurant_service;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class OrderItemDto {

    private UUID orderItemsId;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalPrice;

    private DishDTO dish;

}
