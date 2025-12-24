package com.dineswift.restaurant_service.repository;

import com.dineswift.restaurant_service.model.Menu;
import com.dineswift.restaurant_service.payload.response.dish.DishesForMenu;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> , JpaSpecificationExecutor<Menu> {

    boolean existsByMenuName(String menuName);

    @EntityGraph(
        attributePaths = {
                "dishes"
        })
    @Query("SELECT m FROM Menu m where menuId=:menuId AND isActive=true")
    Optional<Menu> findByIdAndIsActive(@Param("menuId") UUID menuId);

    List<Menu> findAllByRestaurant_RestaurantIdAndIsActive(UUID restaurantId, boolean isActive);


    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Menu m WHERE m.menuName = :menuName AND m.restaurant.restaurantId = :restaurantId AND m.isActive = true")
    boolean existsByMenuNameAndRestaurantId(String menuName, UUID restaurantId);

    @Query(
        "SELECT m.restaurant.restaurantId FROM Menu m WHERE m.menuId = :menuId"
    )
    UUID getRestaurantIdByMenuId(UUID menuId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Menu m WHERE m.menuName = :menuName AND m.restaurant.restaurantId = :restaurantId AND m.isActive = true AND m.menuId <> :menuId")
    boolean existsByMenuNameAndRestaurantIdWithMenuId(String menuName, UUID menuId, UUID restaurantId);


    @Query("""
        SELECT new com.dineswift.restaurant_service.payload.response.dish.DishesForMenu(
            d.dishId, d.dishName, d.dishPrice
        )
        FROM Menu m
        JOIN m.dishes d
        WHERE m.menuId = :menuId
    """)
    List<DishesForMenu> findDishesWithMenuId(UUID menuId);
}
