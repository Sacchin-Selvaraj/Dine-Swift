package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID> {

}
