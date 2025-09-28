package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.dto.RestaurantDTO;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
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

    @PatchMapping("/edit-details/{restaurantId}")
    public ResponseEntity<RestaurantDTO> editRestaurantDetails(@PathVariable UUID restaurantId, @Valid @RequestBody RestaurantUpdateRequest restaurantUpdateRequest) {
        RestaurantDTO updatedRestaurant = restaurantService.editRestaurantDetails(restaurantId, restaurantUpdateRequest);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @PatchMapping("/deactivate/{restaurantId}")
    public ResponseEntity<String> deactivateRestaurant(@PathVariable UUID restaurantId) {
        restaurantService.deactivateRestaurant(restaurantId);
        return ResponseEntity.ok("Restaurant deactivated successfully");
    }

    @PatchMapping("/change-status/{restaurantId}")
    public ResponseEntity<String> changeRestaurantStatus(@PathVariable UUID restaurantId, @RequestParam String status) {
        restaurantService.changeRestaurantStatus(restaurantId, status);
        return ResponseEntity.ok("Restaurant status changed successfully");
    }
}
