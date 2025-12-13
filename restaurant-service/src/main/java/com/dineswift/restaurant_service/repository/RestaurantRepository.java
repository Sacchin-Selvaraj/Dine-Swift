package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.projection.RestaurantTimings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID>, JpaSpecificationExecutor<Restaurant> {

    @Query("SELECT r FROM Restaurant r where restaurantId=:restaurantId AND isActive=true")
    Optional<Restaurant> findByIdAndIsActive(@Param("restaurantId") UUID restaurantId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Restaurant r WHERE r.restaurantId = :restaurantId AND r.isActive = true")
    boolean existsByIdAndIsActive(UUID restaurantId);

    @Query("""
        SELECT r.openingTime AS openingTime, r.closingTime AS closingTime
        FROM Restaurant r
        WHERE r.restaurantId = :restaurantId
        """)
    RestaurantTimings findRestaurantTimingsById(UUID restaurantId);
}
