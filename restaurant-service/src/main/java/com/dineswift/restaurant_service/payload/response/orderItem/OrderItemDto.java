package com.dineswift.restaurant_service.payload.response.orderItem;

import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
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
