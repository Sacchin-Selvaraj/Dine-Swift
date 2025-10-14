package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.model.TableBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, UUID> {

    @Query("SELECT tb FROM TableBooking tb WHERE tb.restaurantTable = :restaurantTable AND tb.isActive = true AND tb.bookingDate = :bookingDate")
    List<TableBooking> findByRestaurantTableAndIsActiveAndBookingDate(RestaurantTable restaurantTable, LocalDate bookingDate);

    @Query("SELECT tb FROM TableBooking tb WHERE tb.tableBookingId = :tableBookingId AND tb.isActive = true")
    Optional<TableBooking> findByIdAndIsActive(UUID tableBookingId);
}
