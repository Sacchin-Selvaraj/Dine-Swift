package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> , JpaSpecificationExecutor<Menu> {

    boolean existsByMenuName(String menuName);

    @Query("SELECT m FROM Menu m where menuId=:menuId AND isActive=true")
    Optional<Menu> findByIdAndIsActive(@Param("menuId") UUID menuId);

    List<Menu> findAllByRestaurant_RestaurantIdAndIsActive(UUID restaurantId, boolean isActive);


}
