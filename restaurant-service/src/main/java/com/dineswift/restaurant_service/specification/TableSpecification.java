package com.dineswift.restaurant_service.specification;

import com.dineswift.restaurant_service.model.RestaurantTable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class TableSpecification {

    public Specification<RestaurantTable> getRestaurantTableSpecification(UUID restaurantId) {
        if (restaurantId==null){
            return Specification.allOf();
        }
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("restaurant").get("restaurantId"), restaurantId);
    }

    public Specification<RestaurantTable> isActive() {
        return ((root, query, criteriaBuilder) -> {
            return criteriaBuilder.isTrue(root.get("isActive"));
        });
    }
}
