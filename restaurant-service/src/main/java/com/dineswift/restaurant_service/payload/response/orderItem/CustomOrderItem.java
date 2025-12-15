package com.dineswift.restaurant_service.payload.response.orderItem;

import lombok.Data;

import java.util.List;

@Data
public class CustomOrderItem {

    private List<OrderItemDto> orderItemDtos;

    public CustomOrderItem(List<OrderItemDto> orderItemDtos) {
        this.orderItemDtos = orderItemDtos;
    }

    public CustomOrderItem() {
    }
}
