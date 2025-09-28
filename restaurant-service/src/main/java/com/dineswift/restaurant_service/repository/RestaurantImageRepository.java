package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, UUID> {

    List<RestaurantImage> findByRestaurantId(UUID restaurantId);

    Optional<RestaurantImage> findByRestaurantIdAndIsPrimary(UUID restaurantId, Boolean isPrimary);

    Optional<RestaurantImage> findByPublicId(String publicId);

    void deleteByPublicId(String publicId);
}