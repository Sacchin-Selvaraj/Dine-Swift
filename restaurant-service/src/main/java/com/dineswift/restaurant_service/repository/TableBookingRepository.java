package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.model.TableBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, UUID> {

    @Query("SELECT tb FROM TableBooking tb WHERE tb.restaurantTable = :restaurantTable AND tb.isActive = true")
    List<TableBooking> findByRestaurantTableAndIsActive(RestaurantTable restaurantTable);
}
