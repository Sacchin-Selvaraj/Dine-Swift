package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.DishException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.DishMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishMapper dishMapper;

    public String addDish(DishAddRequest dishAddRequest, UUID restaurantId) {

        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId).orElseThrow(()-> new RestaurantException("Restaurant not found with id: "+restaurantId));

        Dish dish=dishMapper.toEntity(dishAddRequest);
        dish.setRestaurant(restaurant);
        dishRepository.save(dish);
        log.info("Dish added successfully: {}", dish.getDishName());
        return "Dish added successfully";
    }

    public String deleteDish(UUID dishId) {
        Dish dish = dishRepository.findByIdAndIsActive(dishId).orElseThrow(() -> new RestaurantException("Dish not found with id: " + dishId));
        dish.setIsActive(false);
        dishRepository.save(dish);
        log.info("Dish deleted successfully: {}", dish.getDishName());
        return "Dish deleted successfully";
    }

    public DishDTO updateDish(UUID dishId, @Valid DishUpdateRequest dishUpdateRequest) {

        Dish dish = dishRepository.findById(dishId).orElseThrow(()-> new DishException("Dish not found with id: "+dishId));

        dish = dishMapper.toEntity(dishUpdateRequest, dish);
        Dish updatedDish = dishRepository.save(dish);
        log.info("Dish updated successfully: {}", updatedDish.getDishName());
        return dishMapper.toDTO(updatedDish);
    }

    public Page<DishDTO> searchDishes(Integer pageNo, Integer pageSize, String sortBy,
                                      String sortDir, String dishName, Double minPrice,
                                      Double maxPrice, Double minRating, Double maxRating,
                                      Double discount, Boolean isVeg) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();

            Pageable pageable = PageRequest.of(pageNo,pageSize,sort);

            Specification<Dish> spec = Specification.<Dish>allOf().
                    and(DishSpecification.hasDishName(dishName)).
                    and(DishSpecification.hasMinPrice(minPrice).
                    and(DishSpecification.hasMaxPrice(maxPrice))).
                    and(DishSpecification.hasDishMinRating(minRating)).
                    and(DishSpecification.hasDishMaxRating(maxRating)).
                    and(DishSpecification.hasDiscount(discount)).
                    and(DishSpecification.isVeg(isVeg));

            Page<Dish> dishes = dishRepository.findAll(spec, pageable);

            if (dishes.getContent().isEmpty()){
                return Page.empty();
            }else {
                return dishes.map(dishMapper::toDTO);
            }
        } catch (Exception e) {
            throw new DishException("Dish retrieval failed: " + e.getMessage());
        }

    }
}
