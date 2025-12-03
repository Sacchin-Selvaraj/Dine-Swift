package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantTable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, UUID> {

    boolean existsByTableNumber(String tableNumber);

    List<RestaurantTable> findByRestaurantAndIsActiveTrue(Restaurant restaurant);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.isActive = true")
    Page<RestaurantTable> findAllAndIsActive(Pageable pageable);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.tableId = :tableId AND rt.isActive = true")
    Optional<RestaurantTable> findByIdAndIsActive(UUID tableId);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.tableNumber = :tableNumber AND rt.isActive = true")
    Optional<RestaurantTable> findByTableNumber(String tableNumber);

    @Query("SELECT rt FROM RestaurantTable rt WHERE rt.restaurant = :restaurant AND rt.isActive = true")
    Page<RestaurantTable> findAllByRestaurantAndIsActive(Pageable pageable, Restaurant restaurant);
}
