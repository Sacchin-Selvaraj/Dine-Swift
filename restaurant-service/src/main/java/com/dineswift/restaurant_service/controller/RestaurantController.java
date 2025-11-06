package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.dto.RestaurantDto;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import com.dineswift.restaurant_service.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/create-restaurant/{employeeId}")
    public ResponseEntity<String> createRestaurant(@Valid @RequestBody RestaurantCreateRequest restaurantCreateRequest, @PathVariable UUID employeeId) {
        restaurantService.createRestaurant(restaurantCreateRequest, employeeId);
        return ResponseEntity.ok("Restaurant created successfully");
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @PatchMapping("/edit-details/{restaurantId}")
    public ResponseEntity<RestaurantDto> editRestaurantDetails(@PathVariable UUID restaurantId, @Valid @RequestBody RestaurantUpdateRequest restaurantUpdateRequest) {
        RestaurantDto updatedRestaurant = restaurantService.editRestaurantDetails(restaurantId, restaurantUpdateRequest);
        return ResponseEntity.ok(updatedRestaurant);
    }

    @GetMapping("/get-restaurants")
    public ResponseEntity<Page<RestaurantDto>> getRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "restaurantName") String sortBy,
            @RequestParam(required = false) String restaurantStatus,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String restaurantName,
            @RequestParam(required = false)LocalTime openingTime,
            @RequestParam(required = false)LocalTime closingTime
            ) {
        Page<RestaurantDto> restaurants = restaurantService.getRestaurants(page, size, restaurantStatus, sortDir,sortBy, area, city, district, state, country, restaurantName, openingTime, closingTime);
        return ResponseEntity.ok(restaurants);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/deactivate/{restaurantId}")
    public ResponseEntity<String> deactivateRestaurant(@PathVariable UUID restaurantId) {
        restaurantService.deactivateRestaurant(restaurantId);
        return ResponseEntity.ok("Restaurant deactivated successfully");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @PostMapping("/change-status/{restaurantId}")
    public ResponseEntity<String> changeRestaurantStatus(@PathVariable UUID restaurantId, @RequestParam("status") String status) {
        restaurantService.changeRestaurantStatus(restaurantId, status);
        return ResponseEntity.ok("Restaurant status changed successfully");
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @PostMapping("upload-image/{restaurantId}")
    public CompletableFuture<ResponseEntity<String>> uploadRestaurantImage(@PathVariable UUID restaurantId,@RequestParam("imageFile") MultipartFile imageFile) throws ExecutionException, InterruptedException {
        CompletableFuture<Void> result = restaurantService.uploadRestaurantImage(restaurantId, imageFile);
        return result.thenApply(res-> new ResponseEntity<>("Image uploaded successfully", HttpStatus.ACCEPTED));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("delete-image/{imageId}")
    public CompletableFuture<ResponseEntity<String>> deleteRestaurantImage(@PathVariable UUID imageId) {
        CompletableFuture<Void> result = restaurantService.deleteRestaurantImage(imageId);
        return result.thenApply(res -> ResponseEntity.ok("Image deleted successfully"));
    }

    @GetMapping("/get-images/{restaurantId}")
    public ResponseEntity<?> getRestaurantImages(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantImages(restaurantId));
    }
}
