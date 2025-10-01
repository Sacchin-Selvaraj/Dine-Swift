package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping("/add-dish/{restaurantId}")
    public ResponseEntity<String> addDish(@Valid @RequestBody DishAddRequest dishAddRequest, UUID restaurantId) {
        String response = dishService.addDish(dishAddRequest,restaurantId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-dish/{dishId}")
    public ResponseEntity<String> deleteDish(@PathVariable UUID dishId) {
        String response = dishService.deleteDish(dishId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-dish/{dishId}")
    public ResponseEntity<DishDTO> updateDish(@PathVariable UUID dishId, @Valid @RequestBody DishUpdateRequest dishUpdateRequest) {
        DishDTO updatedDish = dishService.updateDish(dishId, dishUpdateRequest);
        return ResponseEntity.ok(updatedDish);
    }

}
