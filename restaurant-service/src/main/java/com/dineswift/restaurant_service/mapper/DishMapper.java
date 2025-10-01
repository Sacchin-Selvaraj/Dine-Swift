package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.payload.request.DishAddRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DishMapper {

    private final ModelMapper mapper;


    public Dish toEntity(DishAddRequest dishAddRequest) {
        Dish dish = mapper.map(dishAddRequest, Dish.class);
        dish.setDishStarRating(BigDecimal.valueOf(0.0));
        dish.setDishTotalRating(0);
        dish.setDishTotalRatingCount(0);
        return dish;
    }
}
