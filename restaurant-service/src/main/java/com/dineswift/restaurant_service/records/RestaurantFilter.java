package com.dineswift.restaurant_service.records;

import java.time.LocalTime;
import java.util.UUID;

public record RestaurantFilter(
        int page,
        int size,
        String sortDir,
        String sortBy,
        UUID restaurantId,
        String restaurantStatus,
        String area,
        String city,
        String district,
        String state,
        String country,
        String restaurantName,
        LocalTime openingTime,
        LocalTime closingTime
) {
}
