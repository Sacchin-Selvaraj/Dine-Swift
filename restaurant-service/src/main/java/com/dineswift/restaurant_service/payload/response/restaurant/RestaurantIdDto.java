package com.dineswift.restaurant_service.payload.response.restaurant;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RestaurantIdDto {
    private UUID restaurantId;

    public RestaurantIdDto(UUID restaurantId) {
        this.restaurantId = restaurantId;
    }
}
