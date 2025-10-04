package com.dineswift.restaurant_service.service;


import com.dineswift.restaurant_service.exception.DishException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.DishMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.DishImage;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.repository.DishImageRepository;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishMapper dishMapper;
    private final ImageService imageService;
    private final DishImageRepository dishImageRepository;

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
                    and(DishSpecification.isVeg(isVeg))
                    .and(DishSpecification.isActive(true));

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

    public CompletableFuture<Void> uploadRestaurantImage(UUID dishId, MultipartFile imageFile) {
        if (dishId == null || imageFile == null) {
            throw new DishException("Invalid request to upload image");
        }

         return imageService.uploadImage(imageFile,"dish").thenAcceptAsync(res->{
            if (res!=null && (Boolean) res.get("isSuccessful")){
                saveDishImageDetails(dishId,res);
                log.info("Image uploaded successfully for dish id: {}", dishId);
            }else {
                String error = res != null ? (String) res.get("error") : "Unknown error";
                log.error("Image upload failed for dish id: {}. Error: {}", dishId, error);
                throw new DishException(error);
            }
        }).exceptionally(throwable -> {
            log.error("Image upload failed for dish id: {}. Error: {}", dishId, throwable.getMessage());
            throw new DishException("Image upload failed: " + throwable.getMessage());
        });
    }

    private void saveDishImageDetails(UUID dishId, Map<String, Object> res) {
        Dish dish = dishRepository.findByIdAndIsActive(dishId).orElseThrow(() -> new DishException("Dish not found with id: " + dishId));
        log.info("Saving image details for dish: {}", dish.getDishName());
        DishImage dishImage = dishMapper.toImageEntity(res, dish);
        dishImageRepository.save(dishImage);
    }

    public CompletableFuture<Void> deleteRestaurantImage(UUID imageId) {
        if (imageId == null) {
            return CompletableFuture.failedFuture(new DishException("Invalid request to delete image"));
        }
        DishImage dishImage = dishImageRepository.findById(imageId).orElseThrow(() -> new DishException("Image not found with id: " + imageId));

        return imageService.deleteImage(dishImage.getPublicId()).thenAcceptAsync(res -> {
            if (res != null && (Boolean) res.get("isSuccessful")) {
                dishImageRepository.delete(dishImage);
                log.info("Image deleted successfully for image id: {}", imageId);
            } else {
                String error = res != null ? (String) res.get("error") : "Unknown error";
                log.error("Image deletion failed for image id: {}. Error: {}", imageId, error);
                 throw new DishException(error);
            }
        }).exceptionally(throwable -> {
            log.error("Image deletion failed for image id: {}. Error: {}", imageId, throwable.getMessage());
            throw new DishException("Image deletion failed: " + throwable.getMessage());
        });

    }

    public void addRating(UUID dishId, Double rating) {
        if (dishId == null || rating == null) {
            throw new DishException("Invalid request to rate dish");
        }
         Dish dish=dishRepository.findByIdAndIsActive(dishId).orElseThrow(() -> new DishException("Dish not found with id: " + dishId));

        dish.setDishTotalRatingCount(dish.getDishTotalRatingCount()+1);
        dish.setDishTotalRating(dish.getDishTotalRating()+rating);
        dish.setDishStarRating(BigDecimal.valueOf(dish.getDishTotalRating()/dish.getDishTotalRatingCount()));
        dishRepository.save(dish);
        log.info("Rating added successfully for dish id: {}", dishId);
    }
}
