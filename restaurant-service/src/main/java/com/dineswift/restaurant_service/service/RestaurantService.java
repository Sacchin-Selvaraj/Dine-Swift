package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.RestaurantMapper;
import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
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
}
