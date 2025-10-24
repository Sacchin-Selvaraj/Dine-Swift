package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findAllByCartId(UUID cartId);

    Optional<OrderItem> findByCartIdAndDish(UUID cartId, Dish dish);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.tableBooking.tableBookingId = :tableBookingId")
    Page<OrderItem> findAllByTableBookingId(UUID tableBookingId, Pageable pageable);
}
