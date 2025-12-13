package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Dish;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DishRepository extends JpaRepository<Dish, UUID>, JpaSpecificationExecutor<Dish> {


    @Query("SELECT d FROM Dish d WHERE d.id = :dishId AND d.isActive = true")
    Optional<Dish> findByIdAndIsActive(@Param("dishId") UUID dishId);

    boolean existsByDishNameAndRestaurant_RestaurantId(String dishName, UUID restaurantId);



    @Modifying
    @Query(
            """
       UPDATE Dish d 
       SET 
         d.dishTotalRating = d.dishTotalRating + :rating,
         d.dishTotalRatingCount = d.dishTotalRatingCount + 1,
         d.dishStarRating = (d.dishTotalRating + :rating) / (d.dishTotalRatingCount + 1)
       WHERE d.dishId = :dishId AND d.isActive = true
       """
    )
    int updateDishRating(UUID dishId, Double rating);

    @Query("SELECT d FROM Dish d JOIN FETCH d.restaurant WHERE d.dishId = :dishId AND d.isActive = true")
    Optional<Dish> findByIdAndIsActiveWithRestaurant(UUID dishId);
}
