package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantStatus;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestaurantMapper {

    private final ModelMapper mapper;

    public Restaurant toEntity(RestaurantCreateRequest restaurantCreateRequest, Employee employee) {

        Restaurant restaurant=mapper.map(restaurantCreateRequest,Restaurant.class);

        restaurant.setOwnerName(employee.getEmployeeName());
        restaurant.setRestaurantStatus(RestaurantStatus.CREATED);
        restaurant.setLastModifiedBy(employee.getEmployeeId());
        return restaurant;
    }
}
