package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @EntityGraph(attributePaths = {"dish"})
    List<OrderItem> findAllByCartId(UUID cartId);

    @EntityGraph(attributePaths = {"dish"})
    Optional<OrderItem> findByCartIdAndDish(UUID cartId, Dish dish);

    @EntityGraph(attributePaths = {"dish"})
    @Query("SELECT oi FROM OrderItem oi WHERE oi.tableBooking.tableBookingId = :tableBookingId")
    Page<OrderItem> findAllByTableBookingId(UUID tableBookingId, Pageable pageable);

    @EntityGraph(attributePaths = {"dish"})
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderItemsId = :orderItemId")
    Optional<OrderItem> findByIdAndDish(UUID orderItemId);

    @EntityGraph(attributePaths = {"dish"})
    @Query("SELECT oi FROM OrderItem oi WHERE oi.cartId = :cartId")
    List<OrderItem> findAllByCartIdWithDish(UUID cartId);


    @Query("""
        SELECT DISTINCT oi.restaurant.restaurantId
        FROM OrderItem oi
        WHERE oi.orderItemsId IN :orderItemIds
    """)
    Set<UUID> findDistinctRestaurantIdsByOrderItemIds(@Param("orderItemIds") List<UUID> orderItemIds);

    @EntityGraph(attributePaths = {"dish", "tableBooking"})
    @Query("SELECT oi FROM OrderItem oi WHERE oi.orderItemsId = :orderItemsId")
    Optional<OrderItem> findByIdWithDishAndBooking(UUID orderItemsId);

    @Query("SELECT oi.restaurant.restaurantId FROM OrderItem oi WHERE oi.orderItemsId = :orderItemId")
    Optional<UUID> findRestaurantIdByOrderItemId(UUID orderItemId);
}
