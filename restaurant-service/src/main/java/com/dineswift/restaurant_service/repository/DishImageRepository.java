package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.DishImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DishImageRepository extends JpaRepository<DishImage, UUID> {

    List<DishImage> findByDish(Dish updatedDish);
}
