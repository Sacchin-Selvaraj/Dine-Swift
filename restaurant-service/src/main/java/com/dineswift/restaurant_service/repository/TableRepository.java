package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, UUID>, JpaSpecificationExecutor<RestaurantTable> {

    boolean existsByTableNumber(String tableNumber);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.tableId = :tableId AND rt.isActive = true")
    Optional<RestaurantTable> findByIdAndIsActive(UUID tableId);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.tableNumber = :tableNumber AND rt.isActive = true")
    Optional<RestaurantTable> findByTableNumber(String tableNumber);

    @EntityGraph(attributePaths = "restaurant")
    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.tableId = :tableId AND rt.isActive = true")
    Optional<RestaurantTable> findByIdAndIsActiveWithRestaurant(UUID tableId);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.restaurant.restaurantId = :restaurantId AND rt.isActive = true")
    List<RestaurantTable> findByRestaurantIdAndIsActiveTrue(UUID restaurantId);


}
