package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantStatus;
import com.dineswift.restaurant_service.payload.dto.RestaurantDTO;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import jakarta.validation.Valid;
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

    public Restaurant updateEntity(Restaurant restaurant, @Valid RestaurantUpdateRequest restaurantUpdateRequest) {
        if (restaurantUpdateRequest.getRestaurantName() != null)
            restaurant.setRestaurantName(restaurantUpdateRequest.getRestaurantName());
        if (restaurantUpdateRequest.getRestaurantDescription() != null)
            restaurant.setRestaurantDescription(restaurantUpdateRequest.getRestaurantDescription());
        if (restaurantUpdateRequest.getAddress() != null)
            restaurant.setAddress(restaurantUpdateRequest.getAddress());
        if (restaurantUpdateRequest.getArea() != null)
            restaurant.setArea(restaurantUpdateRequest.getArea());
        if (restaurantUpdateRequest.getCity() != null)
            restaurant.setCity(restaurantUpdateRequest.getCity());
        if (restaurantUpdateRequest.getDistrict() != null)
            restaurant.setDistrict(restaurantUpdateRequest.getDistrict());
        if (restaurantUpdateRequest.getState() != null)
            restaurant.setState(restaurantUpdateRequest.getState());
        if (restaurantUpdateRequest.getCountry() != null)
            restaurant.setCountry(restaurantUpdateRequest.getCountry());
        if (restaurantUpdateRequest.getPincode() != null)
            restaurant.setPincode(restaurantUpdateRequest.getPincode());
        if (restaurantUpdateRequest.getWebsiteLink() != null)
            restaurant.setWebsiteLink(restaurantUpdateRequest.getWebsiteLink());
        if (restaurantUpdateRequest.getOpeningTime() != null)
            restaurant.setOpeningTime(restaurantUpdateRequest.getOpeningTime());
        if (restaurantUpdateRequest.getClosingTime() != null)
            restaurant.setClosingTime(restaurantUpdateRequest.getClosingTime());

        // need to set latitude and longitude from address using geocoding service in future

        return restaurant;
    }

    public RestaurantDTO toDTO(Restaurant restaurant) {
        return mapper.map(restaurant, RestaurantDTO.class);
    }
}
