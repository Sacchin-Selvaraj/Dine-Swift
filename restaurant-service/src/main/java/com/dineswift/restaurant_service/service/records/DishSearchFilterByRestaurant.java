package com.dineswift.restaurant_service.service.records;

import java.util.UUID;

public record DishSearchFilterByRestaurant(
        UUID restaurantId,
        Integer pageNo,
        Integer pageSize,
        String sortBy,
        String sortDir,
        String dishName,
        Double minPrice,
        Double maxPrice,
        Double minRating,
        Double maxRating,
        Double discount,
        Boolean isVeg
) {

}
