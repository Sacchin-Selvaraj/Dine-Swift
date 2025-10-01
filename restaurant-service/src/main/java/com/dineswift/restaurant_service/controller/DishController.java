package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.DishAddRequest;
import com.dineswift.restaurant_service.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
