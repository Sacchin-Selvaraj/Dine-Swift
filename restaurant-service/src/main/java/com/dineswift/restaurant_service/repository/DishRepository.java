package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

    @Query("SELECT d FROM Dish d WHERE d.id = :dishId AND d.isActive = true")
    Optional<Dish> findByIdAndIsActiveTrue(@Param("dishId") UUID dishId);
}
