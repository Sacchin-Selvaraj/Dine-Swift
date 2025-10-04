package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.service.DishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping("/add-dish/{restaurantId}")
    public ResponseEntity<String> addDish(@Valid @RequestBody DishAddRequest dishAddRequest, @PathVariable  UUID restaurantId) {
        String response = dishService.addDish(dishAddRequest,restaurantId);
        return new ResponseEntity<>(response,HttpStatus.CREATED);
    }

    @DeleteMapping("/delete-dish/{dishId}")
    public ResponseEntity<String> deleteDish(@PathVariable UUID dishId) {
        String response = dishService.deleteDish(dishId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/update-dish/{dishId}")
    public ResponseEntity<DishDTO> updateDish(@PathVariable UUID dishId, @Valid @RequestBody DishUpdateRequest dishUpdateRequest) {
        DishDTO updatedDish = dishService.updateDish(dishId, dishUpdateRequest);
        return ResponseEntity.ok(updatedDish);
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

    @PostMapping("upload-image/{dishId}")
    public CompletableFuture<ResponseEntity<String>> uploadRestaurantImage(@PathVariable UUID dishId,@RequestParam("imageFile") MultipartFile imageFile) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> result = dishService.uploadRestaurantImage(dishId, imageFile);
        return result.thenApply(res-> ResponseEntity.ok("Image uploaded successfully"));
    }

    @DeleteMapping("delete-image/{imageId}")
    public CompletableFuture<ResponseEntity<String>> deleteRestaurantImage(@PathVariable UUID imageId) {
        CompletableFuture<Void> result = dishService.deleteRestaurantImage(imageId);
        return result.thenApply(res -> ResponseEntity.ok("Image deleted successfully"));
    }

    @GetMapping("/get-images/{dishId}")
    public ResponseEntity<?> getRestaurantImages(@PathVariable UUID dishId) {
        return ResponseEntity.ok(dishService.getDishImages(dishId));
    }
}
