package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.UUID;

public final class RestaurantSpecification {

    /**
     * Specification for filtering by exact match on restaurant status.
     *
     * @param status The restaurant status (e.g., "OPEN").
     */
    public static Specification<Restaurant> hasStatus(RestaurantStatus status) {
        // If the parameter is null, we return a specification that imposes no restriction (where true)
        if (status == null) {
            return Specification.allOf();
        }
        return (root, query, builder) -> builder.equal(root.get("restaurantStatus"), status);
    }

    /**
     * Specification for filtering by partial, case-insensitive match on restaurant name.
     * @param name The name fragment to search for.
     */
    public static Specification<Restaurant> nameContains(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Specification.allOf();
        }
        String pattern = "%" + name.toLowerCase() + "%";
        return (root, query, builder) ->
                builder.like(builder.lower(root.get("restaurantName")), pattern);
    }

    // --- Address Specifications (Exact Match) ---

    public static Specification<Restaurant> hasArea(String area) {
        return filterByStringField("area", area);
    }

    public static Specification<Restaurant> hasCity(String city) {
        return filterByStringField("city", city);
    }

    public static Specification<Restaurant> hasDistrict(String district) {
        return filterByStringField("district", district);
    }

    public static Specification<Restaurant> hasState(String state) {
        return filterByStringField("state", state);
    }

    public static Specification<Restaurant> hasCountry(String country) {
        return filterByStringField("country", country);
    }

    private static Specification<Restaurant> filterByStringField(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            return Specification.allOf();
        }
        // Case-insensitive exact match for location fields
        String pattern = "%" + value.toLowerCase() + "%";
        return (root, query, builder) ->
                builder.like(builder.lower(root.get(fieldName)), pattern);
    }

    // --- Time Specifications ---

    /**
     * Filter restaurants that are open BEFORE the requested time.
     * This checks if the restaurant's opening time is <= the requested time.
     */
    public static Specification<Restaurant> openBefore(LocalTime time) {
        if (time == null) {
            return Specification.allOf();
        }
        // opening_time => requested_time
        return (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("openingTime"), time);
    }

    /**
     * Filter restaurants that close AFTER the requested time.
     * This checks if the restaurant's closing time is >= the requested time.
     */
    public static Specification<Restaurant> closeAfter(LocalTime time) {
        if (time == null) {
            return Specification.allOf();
        }
        // closing_time <= requested_time
        return (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("closingTime"), time);
    }

    public static Specification<Restaurant> hasId(UUID restaurantId) {
        if (restaurantId == null) {
            return Specification.allOf();
        }
        return (root, query, builder)
                -> builder.equal(root.get("restaurantId"), restaurantId);

    }
}
