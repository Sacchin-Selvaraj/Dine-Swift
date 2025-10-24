package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID>, JpaSpecificationExecutor<Dish> {

    @Query("SELECT d FROM Dish d WHERE d.id = :dishId AND d.isActive = true")
    Optional<Dish> findByIdAndIsActive(@Param("dishId") UUID dishId);

    @Query("SELECT d FROM Dish d WHERE d.restaurant.restaurantId = :restaurantId AND d.isActive = true")
    Page<Dish> findAllByRestaurant(Specification<Dish> spec, Pageable pageable, UUID restaurantId);
}
