package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.RestaurantTable;
import com.dineswift.restaurant_service.model.TableBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TableBookingRepository extends JpaRepository<TableBooking, UUID>, JpaSpecificationExecutor<TableBooking> {

    @Query("SELECT tb FROM TableBooking tb WHERE tb.restaurantTable = :restaurantTable AND tb.isActive = true AND tb.bookingDate = :bookingDate")
    List<TableBooking> findByRestaurantTableAndIsActiveAndBookingDate(RestaurantTable restaurantTable, LocalDate bookingDate);

    @Query("SELECT tb FROM TableBooking tb WHERE tb.tableBookingId = :tableBookingId AND tb.isActive = true")
    Optional<TableBooking> findByIdAndIsActive(UUID tableBookingId);

    @EntityGraph(attributePaths = "restaurant")
    @Query("SELECT tb FROM TableBooking tb WHERE tb.tableBookingId = :tableBookingId AND tb.isActive = true")
    Optional<TableBooking> findByIdWithRestaurant(UUID tableBookingId);

    @EntityGraph(attributePaths = "guestInformation")
    @Query("SELECT tb FROM TableBooking tb WHERE tb.tableBookingId = :tableBookingId AND tb.isActive = true")
    Optional<TableBooking> findByIdWithGuestInformation(UUID tableBookingId);

    @EntityGraph(attributePaths = {"restaurantTable", "restaurant", "guestInformation"})
    @Query("SELECT tb FROM TableBooking tb WHERE tb.tableBookingId = :tableBookingId AND tb.isActive = true")
    Optional<TableBooking> findByIdWithChildClass(UUID tableBookingId);

    @EntityGraph(attributePaths = {"restaurantTable", "guestInformation"})
    Page<TableBooking> findAllByChildEntities(Specification<TableBooking> spec, Pageable pageable);
}
