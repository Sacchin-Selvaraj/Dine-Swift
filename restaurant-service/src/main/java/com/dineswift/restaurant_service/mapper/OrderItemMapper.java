package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import com.dineswift.restaurant_service.payload.response.orderItem.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final DishMapper dishMapper;


    public OrderItem toEntity(UUID cartId, Dish dish, Integer quantity) {
        OrderItem orderItem=new OrderItem();
        orderItem.setCartId(cartId);
        orderItem.setDish(dish);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(dish.getDishPrice());
        orderItem.setTotalPrice(dish.getDishPrice().multiply(BigDecimal.valueOf(quantity)));
        orderItem.setRestaurant(dish.getRestaurant());
        return orderItem;
    }

    public OrderItemDto toDto(OrderItem orderItem) {
        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setOrderItemsId(orderItem.getOrderItemsId());
        orderItemDto.setDish(dishMapper.toDTO(orderItem.getDish()));
        orderItemDto.setQuantity(orderItem.getQuantity());
        orderItemDto.setPrice(orderItem.getPrice());
        orderItemDto.setTotalPrice(orderItem.getTotalPrice());
        return orderItemDto;
    }
}
