package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, UUID> {

    List<RestaurantImage> findByRestaurant(Restaurant restaurant);

    @Query("SELECT ri FROM RestaurantImage ri WHERE ri.restaurant IN :restaurantList")
    List<RestaurantImage> findByRestaurants(List<Restaurant> restaurantList);

    @Query("SELECT ri FROM RestaurantImage ri WHERE ri.imageId = :imageId")
    Optional<RestaurantImage> findByIdAndRestaurant(UUID imageId);

    List<RestaurantImage> findByRestaurant_RestaurantId(UUID restaurantId);

    @Query(
        "SELECT ri.restaurant.restaurantId FROM RestaurantImage ri WHERE ri.imageId = :imageId"
    )
    UUID getRestaurantIdByImageId(UUID imageId);
}