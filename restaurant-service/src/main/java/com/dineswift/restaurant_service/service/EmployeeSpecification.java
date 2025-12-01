package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public final class EmployeeSpecification {

    public Specification<Employee> hasRestaurant(Restaurant restaurant) {
        if (restaurant==null)
            return Specification.allOf();
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("restaurant"), restaurant);
    }

    public Specification<Employee> isActive() {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.isTrue(root.get("employeeIsActive"));
    }
}
