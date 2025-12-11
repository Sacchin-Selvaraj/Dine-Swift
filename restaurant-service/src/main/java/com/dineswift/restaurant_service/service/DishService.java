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
import com.dineswift.restaurant_service.security.service.AuthService;
import com.dineswift.restaurant_service.service.records.DishSearchFilter;
import com.dineswift.restaurant_service.service.records.DishSearchFilterByRestaurant;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishMapper dishMapper;
    private final ImageService imageService;
    private final DishImageRepository dishImageRepository;
    private final AuthService authService;

    @CacheEvict(
            value = {"restaurant:dishes","restaurant:dishesByRestaurant"},
            allEntries = true
    )
    @Transactional
    public void addDish(DishAddRequest dishAddRequest, UUID restaurantId) {

        if (!restaurantRepository.existsByIdAndIsActive(restaurantId)) {
            throw new RestaurantException(
                    "Restaurant not found with id: " + restaurantId);
        }

        if (dishRepository.existsByDishNameAndRestaurant_RestaurantId(
                dishAddRequest.getDishName(), restaurantId)) {
            throw new DishException("Dish already exists");
        }

        Restaurant restaurantRef = restaurantRepository.getReferenceById(restaurantId);

        Dish dish=dishMapper.toEntity(dishAddRequest);
        dish.setRestaurant(restaurantRef);
        dish.setLastModifiedBy(authService.getAuthenticatedId());

        dishRepository.save(dish);

        log.info("Dish added successfully: {}", dish.getDishName());
    }

    @CacheEvict(
            value = {"restaurant:dishes","restaurant:dishesByRestaurant","restaurant-menuDetails"},
            allEntries = true
    )
    @Transactional
    public void deleteDish(UUID dishId) {

        Dish dish = dishRepository.findByIdAndIsActive(dishId)
                .orElseThrow(() -> new RestaurantException("Dish not found with id: " + dishId));

        dish.setIsActive(false);
        dish.setLastModifiedBy(authService.getAuthenticatedId());

        dishRepository.save(dish);

        log.info("Dish deleted successfully: {}", dish.getDishName());
    }

    @CacheEvict(
            value = {"restaurant:dishes",
                    "restaurant:dishesByRestaurant",
                    "restaurant-menuDetails",
                    "restaurant:order-items-by-cart",
                    "restaurant:order-items-by-booking"
            },
            allEntries = true
    )
    @Transactional
    public void updateDish(UUID dishId, @Valid DishUpdateRequest dishUpdateRequest) {

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(()-> new DishException("Dish not found with id: "+dishId));

        dish = dishMapper.toEntity(dishUpdateRequest, dish);
        dish.setLastModifiedBy(authService.getAuthenticatedId());

        Dish updatedDish = dishRepository.save(dish);

        log.info("Dish updated successfully: {}", updatedDish.getDishName());
    }

    @Cacheable(
            value = "restaurant:dishes",
            key = "#filter.hashCode()",
            unless = "#result == null || #result.isEmpty()"
    )
    public CustomPageDto<DishDTO> searchDishes(DishSearchFilter filter) {

        try {
            Sort sort = filter.sortDir().equalsIgnoreCase("asc")?
                    Sort.by(filter.sortBy()).ascending():Sort.by(filter.sortBy()).descending();

            Pageable pageable = PageRequest.of(filter.pageNo(),filter.pageSize(),sort);

            Specification<Dish> spec = Specification.<Dish>allOf().
                    and(DishSpecification.hasDishName(filter.dishName())).
                    and(DishSpecification.hasMinPrice(filter.minPrice()).
                    and(DishSpecification.hasMaxPrice(filter.maxPrice()))).
                    and(DishSpecification.hasDishMinRating(filter.minRating())).
                    and(DishSpecification.hasDishMaxRating(filter.maxRating())).
                    and(DishSpecification.hasDiscount(filter.discount())).
                    and(DishSpecification.isVeg(filter.isVeg())).
                    and(DishSpecification.isActive(true));

            Page<Dish> dishes = dishRepository.findAll(spec, pageable);

            if (dishes.getContent().isEmpty()){
                return new CustomPageDto<>(Page.empty());
            }

            return new CustomPageDto<>(dishes.map(dishMapper::toDTO));

        } catch (Exception e) {
            throw new DishException("Dish retrieval failed: " + e.getMessage());
        }

    }

    @NotNull
    private CustomPageDto<DishDTO> getCustomPageDto(Page<Dish> dishes) {
        List<Dish> dishesList = dishes.getContent();

        List<DishDTO> dishDTOS = dishMapper.toDTOList(dishesList);

        Map<UUID,DishDTO> dishDTOMap = dishDTOS.stream()
                .collect(Collectors.toMap(DishDTO::getDishId, dto->dto));

        Page<DishDTO> dishDTOPage = dishes.map(dish -> dishDTOMap.get(dish.getDishId()));

        return new CustomPageDto<>(dishDTOPage);
    }

    @CacheEvict(
            value = {"restaurant:dishes",
                    "restaurant:dishesByRestaurant",
                    "restaurant-menuDetails",
                    "restaurant:order-items-by-cart",
                    "restaurant:order-items-by-booking"
            },
            allEntries = true
    )
    public void uploadDishImage(UUID dishId, MultipartFile imageFile) {

        log.info("Initiating image upload for dish id: {}", dishId);
        if (dishId == null || imageFile == null || !dishRepository.existsById(dishId)) {
            throw new DishException("Invalid request to upload image");
        }

        imageService.uploadImage(imageFile,"dish").thenAccept(res->{
            if (res!=null && (Boolean) res.get("isSuccessful")){
                saveDishImageDetails(dishId,res);
                log.info("Image uploaded successfully for dish id: {}", dishId);
            }else {
                String error = res != null ? (String) res.get("error") : "Unknown error";
                log.error("Image upload failed for dish id: {}. Error: {}", dishId, error);
                throw new DishException(error);
            }
        }).exceptionally(throwable -> {
            log.error("Image upload failed for dish id: {}. Errors: {}", dishId, throwable.getMessage());
            throw new DishException("Image upload failed: " + throwable.getMessage());
        });
    }

    @Transactional
    private void saveDishImageDetails(UUID dishId, Map<String, Object> res) {
        log.info("Saving image details for dish id: {}", dishId);

        Dish dish = dishRepository.getReferenceById(dishId);
        log.info("Saving image details for dish: {}", dish.getDishName());

        DishImage dishImage = dishMapper.toImageEntity(res, dish);

        dishImageRepository.save(dishImage);
    }

    @CacheEvict(
            value = {"restaurant:dishes",
                    "restaurant:dishesByRestaurant",
                    "restaurant-menuDetails",
                    "restaurant:order-items-by-cart",
                    "restaurant:order-items-by-booking"
            },
            allEntries = true
    )
    public void deleteRestaurantImage(UUID imageId) {
        if (imageId == null) {
            throw new DishException("Invalid request to delete image");
        }
        DishImage dishImage = dishImageRepository.findById(imageId)
                .orElseThrow(() -> new DishException("Image not found with id: " + imageId));

        imageService.deleteImage(dishImage.getPublicId()).thenAcceptAsync(res -> {
            if (res != null && (Boolean) res.get("isSuccessful")) {
                dishImageRepository.delete(dishImage);
                log.info("Image deleted successfully for image id: {}", imageId);
            } else {
                String error = res != null ? (String) res.get("error") : "Unknown error";
                log.error("Image deletion failed for image id: {}. Error: {}", imageId, error);
                 throw new DishException(error);
            }
        }).exceptionally(throwable -> {
            log.error("Image deletion failed for image id: {}. Errors: {}", imageId, throwable.getMessage());
            throw new DishException("Image deletion failed: " + throwable.getMessage());
        });

    }

    @Transactional
    public void addRating(UUID dishId, Double rating) {

        int updated = dishRepository.updateDishRating(dishId, rating);

        if (updated == 0) {
            throw new DishException("Dish not found or inactive: " + dishId);
        }

        log.info("Rating updated successfully for dish id: {}", dishId);
    }

    @Cacheable(
            value = "restaurant:dishesByRestaurant",
            key = "#filter.hashCode()",
            unless = "#result == null || #result.isEmpty()"
    )
    public CustomPageDto<DishDTO> searchDishesByRestaurant(DishSearchFilterByRestaurant filter) {

        boolean restaurantExists = restaurantRepository.existsByIdAndIsActive(filter.restaurantId());
        if (!restaurantExists) {
            throw new RestaurantException("Restaurant not found with id: " + filter.restaurantId());
        }
        try {
            Sort sort = filter.sortDir().equalsIgnoreCase("asc")
                    ?Sort.by(filter.sortBy()).ascending():Sort.by(filter.sortBy()).descending();

            Pageable pageable = PageRequest.of(filter.pageNo(),filter.pageSize(),sort);

            Specification<Dish> spec = Specification.<Dish>allOf().
                    and(DishSpecification.hasRestaurantId(filter.restaurantId())).
                    and(DishSpecification.hasDishName(filter.dishName())).
                    and(DishSpecification.hasMinPrice(filter.minPrice()).
                    and(DishSpecification.hasMaxPrice(filter.maxPrice()))).
                    and(DishSpecification.hasDishMinRating(filter.minRating())).
                    and(DishSpecification.hasDishMaxRating(filter.maxRating())).
                    and(DishSpecification.hasDiscount(filter.discount())).
                    and(DishSpecification.isVeg(filter.isVeg())).
                    and(DishSpecification.isActive(true));

            Page<Dish> dishes = dishRepository.findAll(spec,pageable);

            if (dishes.getContent().isEmpty()){
                return new CustomPageDto<>(Page.empty());
            }
            return getCustomPageDto(dishes);

        } catch (Exception e) {
            throw new DishException("Dish retrieval failed: " + e.getMessage());
        }

    }
}
