package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.payload.response.dish.DishesForMenu;
import com.dineswift.restaurant_service.service.CustomPageDto;
import com.dineswift.restaurant_service.service.DishService;
import com.dineswift.restaurant_service.records.DishSearchFilter;
import com.dineswift.restaurant_service.records.DishSearchFilterByRestaurant;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/restaurant/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("/add-dish/{restaurantId}")
    public ResponseEntity<Void> addDish(
            @Valid @RequestBody DishAddRequest dishAddRequest,
            @PathVariable  UUID restaurantId) {

        dishService.addDish(dishAddRequest,restaurantId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/delete-dish/{dishId}")
    public ResponseEntity<Void> deleteDish(@PathVariable UUID dishId) {

        dishService.deleteDish(dishId);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PatchMapping("/update-dish/{dishId}")
    public ResponseEntity<Void> updateDish(@PathVariable UUID dishId,
                                           @Valid @RequestBody DishUpdateRequest dishUpdateRequest) {

        dishService.updateDish(dishId, dishUpdateRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/search-dish")
    public ResponseEntity<CustomPageDto<DishDTO>> searchDishes(
            @RequestParam(value = "page") Integer pageNo,
            @RequestParam(value = "size") Integer pageSize,
            @RequestParam(value = "sortBy", required = false, defaultValue = "dishName") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "dishName", required = false) String dishName,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "discount", required = false) Double discount,
            @RequestParam(value = "isVeg", required = false) Boolean isVeg
    ){
        DishSearchFilter filter = new DishSearchFilter(
                pageNo, pageSize, sortBy, sortDir,
                dishName, minPrice, maxPrice, minRating, maxRating,
                discount, isVeg
        );
        CustomPageDto<DishDTO> dishDTOS = dishService.searchDishes(filter);
        return ResponseEntity.ok(dishDTOS);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("/upload-image/{dishId}")
    public ResponseEntity<Void> uploadDishImage(@PathVariable UUID dishId,
                                                @RequestParam("imageFile") MultipartFile imageFile) {
        dishService.uploadDishImage(dishId, imageFile);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/delete-image/{imageId}")
    public ResponseEntity<Void> deleteRestaurantImage(@PathVariable UUID imageId) {

        dishService.deleteRestaurantImage(imageId);

        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MANAGER')")
    @PatchMapping("/rate-dish/{dishId}")
    public ResponseEntity<Void> rateDish(@PathVariable UUID dishId, @RequestParam Double rating) {

        dishService.addRating(dishId,rating);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/search-dish-restaurant/{restaurantId}")
    public ResponseEntity<CustomPageDto<DishDTO>> searchDishByRestaurant(
            @PathVariable UUID restaurantId,
            @RequestParam(value = "page") Integer pageNo,
            @RequestParam(value = "size") Integer pageSize,
            @RequestParam(value = "sortBy", required = false, defaultValue = "dishName") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "asc") String sortDir,
            @RequestParam(value = "dishName", required = false) String dishName,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "discount", required = false) Double discount,
            @RequestParam(value = "isVeg", required = false) Boolean isVeg
    ){
        DishSearchFilterByRestaurant filter = new DishSearchFilterByRestaurant(
                restaurantId, pageNo, pageSize, sortBy, sortDir,
                dishName, minPrice, maxPrice, minRating, maxRating,
                discount, isVeg
        );

        CustomPageDto<DishDTO> dishDTOS = dishService.searchDishesByRestaurant(filter);
        return ResponseEntity.ok(dishDTOS);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/get-dish-Ids/{restaurantId}")
    public ResponseEntity<List<DishesForMenu>> getDishDetailsForMenu(@PathVariable UUID restaurantId){
        List<DishesForMenu> dishesForMenus = dishService.getDishDetails(restaurantId);
        return ResponseEntity.ok(dishesForMenus);
    }

    @PreAuthorize("!hasRole('ROLE_USER')")
    @GetMapping("/get-dish-Ids-menuId/{menuId}")
    public ResponseEntity<List<DishesForMenu>> getDishDetailsWithMenu(@PathVariable UUID menuId){
        List<DishesForMenu> dishesForMenus = dishService.getDishDetailsWithMenu(menuId);
        return ResponseEntity.ok(dishesForMenus);
    }

}
