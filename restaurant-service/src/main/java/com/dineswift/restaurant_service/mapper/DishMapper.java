package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
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

    public Dish toEntity(DishUpdateRequest dishUpdateRequest, Dish dish) {
        if (dishUpdateRequest.getDishName()!=null){
            dish.setDishName(dishUpdateRequest.getDishName());
        }
        if (dishUpdateRequest.getDishPrice()!=null){
            dish.setDishPrice(dishUpdateRequest.getDishPrice());
        }
        if (dishUpdateRequest.getDishDescription()!=null){
            dish.setDishDescription(dishUpdateRequest.getDishDescription());
        }
        if (dishUpdateRequest.getIsAvailable()!=null){
            dish.setIsAvailable(dishUpdateRequest.getIsAvailable());
        }
        if (dishUpdateRequest.getDiscount()!=null){
            dish.setDiscount(dishUpdateRequest.getDiscount());
        }
        if (dishUpdateRequest.getDishComments()!=null){
            dish.setDishComments(dishUpdateRequest.getDishComments());
        }
        if (dishUpdateRequest.getIsVeg()!=null){
            dish.setIsVeg(dishUpdateRequest.getIsVeg());
        }
        if (dishUpdateRequest.getIsActive()!=null){
            dish.setIsActive(dishUpdateRequest.getIsActive());
        }
        return dish;
    }

    public DishDTO toDTO(Dish updatedDish) {
        return mapper.map(updatedDish, DishDTO.class);
    }
}
