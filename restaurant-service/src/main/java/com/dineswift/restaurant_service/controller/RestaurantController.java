package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PostMapping("/create-restaurant/{employeeId}")
    public ResponseEntity<String> createRestaurant(@Valid @RequestBody RestaurantCreateRequest restaurantCreateRequest, @PathVariable UUID employeeId) {
        restaurantService.createRestaurant(restaurantCreateRequest, employeeId);
        return ResponseEntity.ok("Restaurant created successfully");
    }

}
