package com.dineswift.restaurant_service.service.specification;


import com.dineswift.restaurant_service.model.Dish;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class DishSpecification {

    public static Specification<Dish> hasRestaurantId(UUID restaurantId) {
        if (restaurantId== null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.equal(root.get("restaurant").get("restaurantId"), restaurantId);
    }

    public static Specification<Dish> hasDishName(String dishName) {
        if (dishName == null || dishName.trim().isEmpty()) {
            return Specification.allOf();
        }
        String pattern = "%" + dishName.toLowerCase() + "%";
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("dishName")), pattern);
    }

    public static  Specification<Dish> hasMinPrice(Double minPrice) {
        if (minPrice == null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("dishPrice"), minPrice);
    }

    public static Specification<Dish> hasMaxPrice(Double maxPrice) {
        if (maxPrice == null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("dishPrice"), maxPrice);
    }

    public static Specification<Dish> hasDishMinRating(Double minRating) {
        if (minRating == null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("dishStarRating"), minRating);
    }

    public static Specification<Dish> hasDishMaxRating(Double maxRating) {
        if (maxRating == null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("dishStarRating"), maxRating);
    }

    public static Specification<Dish> hasDiscount(Double discount) {
        if (discount == null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("discount"), discount);
    }

    public static Specification<Dish> isVeg(Boolean isVeg) {
        if (isVeg == null) {
            return Specification.allOf();
        }
        return (root, query, builder) ->
                builder.equal(root.get("isVeg"), isVeg);
    }

    public static Specification<Dish> isActive(boolean value) {
        return (root, query, builder) ->
                builder.equal(root.get("isActive"), value);
    }
}

