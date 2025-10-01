package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.DishMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.request.DishAddRequest;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishMapper dishMapper;

    public String addDish(DishAddRequest dishAddRequest, UUID restaurantId) {

        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(()-> new RestaurantException("Restaurant not found with id: "+restaurantId));

        Dish dish=dishMapper.toEntity(dishAddRequest);
        dish.setRestaurant(restaurant);
        dishRepository.save(dish);
        log.info("Dish added successfully: {}", dish.getDishName());
        return "Dish added successfully";

    }


}
