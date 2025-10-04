package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    boolean existsByMenuName(String menuName);

    Optional<Menu> findByIdAndIsActive(UUID menuId);

    List<Menu> findAllByRestaurant_RestaurantIdAndIsActive(UUID restaurantId, boolean isActive);
}
