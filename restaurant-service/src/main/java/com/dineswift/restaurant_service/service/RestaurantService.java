package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.RestaurantMapper;
import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantStatus;
import com.dineswift.restaurant_service.payload.dto.RestaurantDTO;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final RestaurantMapper restaurantMapper;

    public void createRestaurant(RestaurantCreateRequest restaurantCreateRequest, UUID employeeId) {
        if (restaurantCreateRequest==null || employeeId==null) {
            throw new RestaurantException("Invalid Restaurant Create data");
        }
        Employee employee=employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found with id: " + employeeId));

        if (employee.getRestaurant()!=null && employee.getRestaurant().getIsActive()){
            throw new RestaurantException("Admin Can have only one Active Restaurant");
        }
        Restaurant restaurant=restaurantMapper.toEntity(restaurantCreateRequest,employee);

        // need to set latitude and longitude from address using geocoding service in future
        employee.setRestaurant(restaurant);

        employeeRepository.save(employee);
    }

    public RestaurantDTO editRestaurantDetails(UUID restaurantId, @Valid RestaurantUpdateRequest restaurantUpdateRequest) {
        if (restaurantId == null || restaurantUpdateRequest == null) {
            throw new RestaurantException("Invalid Restaurant Update data");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        restaurant=restaurantMapper.updateEntity(restaurant, restaurantUpdateRequest);
        restaurantRepository.save(restaurant);
        return restaurantMapper.toDTO(restaurant);

    }

    public void deactivateRestaurant(UUID restaurantId) {
        if (restaurantId == null) {
            throw new RestaurantException("Invalid Restaurant Id");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }


    public void changeRestaurantStatus(UUID restaurantId, String status) {
        if (restaurantId == null || status == null) {
            throw new RestaurantException("Invalid data for changing Restaurant status");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        try {
            RestaurantStatus restaurantStatus = RestaurantStatus.fromDisplayName(status);
            restaurant.setRestaurantStatus(restaurantStatus);
            restaurantRepository.save(restaurant);
        } catch (IllegalArgumentException e) {
            throw new RestaurantException("Invalid status: " + status);
        }
    }
}
