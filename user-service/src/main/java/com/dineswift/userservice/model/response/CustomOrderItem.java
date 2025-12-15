package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.response.restaurant_service.OrderItemDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class CustomOrderItem {

    private List<OrderItemDto> orderItemDtos;

    public CustomOrderItem(List<OrderItemDto> orderItemDtos) {
        this.orderItemDtos = orderItemDtos;
    }
}
