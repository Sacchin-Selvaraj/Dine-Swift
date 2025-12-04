package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/restaurant/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("/add-dish/{restaurantId}")
    public ResponseEntity<MessageResponse> addDish(@Valid @RequestBody DishAddRequest dishAddRequest, @PathVariable  UUID restaurantId) {
        String response = dishService.addDish(dishAddRequest,restaurantId);
        return new ResponseEntity<>(MessageResponse.builder().message(response).build(),HttpStatus.CREATED);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/delete-dish/{dishId}")
    public ResponseEntity<MessageResponse> deleteDish(@PathVariable UUID dishId) {
        String response = dishService.deleteDish(dishId);
        return ResponseEntity.ok(MessageResponse.builder().message(response).build());
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PatchMapping("/update-dish/{dishId}")
    public ResponseEntity<MessageResponse> updateDish(@PathVariable UUID dishId, @Valid @RequestBody DishUpdateRequest dishUpdateRequest) {
        dishService.updateDish(dishId, dishUpdateRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Dish updated successfully").build());
    }

    @GetMapping("/search-dish")
    public ResponseEntity<Page<DishDTO>> searchDishes(
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
        Page<DishDTO> dishDTOS = dishService.searchDishes(pageNo, pageSize, sortBy, sortDir, dishName, minPrice, maxPrice, minRating, maxRating, discount, isVeg);
        return ResponseEntity.ok(dishDTOS);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("/upload-image/{dishId}")
    public ResponseEntity<MessageResponse> uploadRestaurantImage(@PathVariable UUID dishId,@RequestParam("imageFile") MultipartFile imageFile) throws ExecutionException, InterruptedException {
        dishService.uploadRestaurantImage(dishId, imageFile);
        return new ResponseEntity<>(MessageResponse.builder().message("Image uploaded successfully").build(),HttpStatus.ACCEPTED);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/delete-image/{imageId}")
    public ResponseEntity<MessageResponse> deleteRestaurantImage(@PathVariable UUID imageId) {
        dishService.deleteRestaurantImage(imageId);
        return ResponseEntity.ok(new MessageResponse("Image deleted successfully"));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MANAGER')")
    @PatchMapping("/rate-dish/{dishId}")
    public ResponseEntity<Void> rateDish(@PathVariable UUID dishId, @RequestParam Double rating) {
        dishService.addRating(dishId,rating);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/search-dish-restaurant/{restaurantId}")
    public ResponseEntity<Page<DishDTO>> searchDishByRestaurant(
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
        Page<DishDTO> dishDTOS = dishService.searchDishesByRestaurant(restaurantId,pageNo, pageSize, sortBy, sortDir, dishName, minPrice, maxPrice, minRating, maxRating, discount, isVeg);
        return ResponseEntity.ok(dishDTOS);
    }

}
