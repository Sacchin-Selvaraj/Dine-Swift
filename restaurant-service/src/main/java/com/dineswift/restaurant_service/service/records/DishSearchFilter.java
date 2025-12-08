package com.dineswift.restaurant_service.service.records;

public record DishSearchFilter(
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
