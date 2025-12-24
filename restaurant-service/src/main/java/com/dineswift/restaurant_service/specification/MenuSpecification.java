package com.dineswift.restaurant_service.specification;

import com.dineswift.restaurant_service.model.Menu;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public final class MenuSpecification {

    public Specification<Menu> hasRestaurantId(UUID restaurantId) {
        if (restaurantId==null)
            return Specification.allOf();
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("restaurant").get("restaurantId"), restaurantId);
    }

    public Specification<Menu> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("isActive"));
    }
}
