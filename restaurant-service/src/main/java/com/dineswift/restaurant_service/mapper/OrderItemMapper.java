package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class OrderItemMapper {


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
}
