package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.dto.RestaurantDto;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.service.CustomPageDto;
import com.dineswift.restaurant_service.service.RestaurantService;
import com.dineswift.restaurant_service.service.records.RestaurantFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-restaurant")
    public ResponseEntity<Void> createRestaurant(@Valid @RequestBody RestaurantCreateRequest restaurantCreateRequest) {
        restaurantService.createRestaurant(restaurantCreateRequest);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @PatchMapping("/edit-details/{restaurantId}")
    public ResponseEntity<Void> editRestaurantDetails(@PathVariable UUID restaurantId,
                                                                 @Valid @RequestBody RestaurantUpdateRequest restaurantUpdateRequest) {

        restaurantService.editRestaurantDetails(restaurantId, restaurantUpdateRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-restaurants")
    public ResponseEntity<CustomPageDto<RestaurantDto>> getRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "restaurantName") String sortBy,
            @RequestParam(required = false) UUID restaurantId,
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
        RestaurantFilter filter = new RestaurantFilter(
                page,
                size,
                sortDir,
                sortBy,
                restaurantId,
                restaurantStatus,
                area,
                city,
                district,
                state,
                country,
                restaurantName,
                openingTime,
                closingTime
        );
        CustomPageDto<RestaurantDto> restaurants = restaurantService.getRestaurants(filter);
        return ResponseEntity.ok(restaurants);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/deactivate/{restaurantId}")
    public ResponseEntity<Void> deactivateRestaurant(@PathVariable UUID restaurantId) {
        restaurantService.deactivateRestaurant(restaurantId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @PostMapping("/change-status/{restaurantId}")
    public ResponseEntity<Void> changeRestaurantStatus(@PathVariable UUID restaurantId,
                                                                  @RequestParam("status") String status) {
        restaurantService.changeRestaurantStatus(restaurantId, status);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @PostMapping("/upload-image/{restaurantId}")
    public ResponseEntity<Void> uploadRestaurantImage(@PathVariable UUID restaurantId,
                                                                 @RequestParam("imageFile") MultipartFile imageFile) throws ExecutionException, InterruptedException {

        restaurantService.uploadRestaurantImage(restaurantId, imageFile);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @DeleteMapping("/delete-image/{imageId}")
    public ResponseEntity<MessageResponse> deleteRestaurantImage(@PathVariable UUID imageId) {
        restaurantService.deleteRestaurantImage(imageId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-images/{restaurantId}")
    public ResponseEntity<?> getRestaurantImages(@PathVariable UUID restaurantId) {
        return ResponseEntity.ok(restaurantService.getRestaurantImages(restaurantId));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_MANAGER')")
    @GetMapping("/get-employee-restaurant")
    public ResponseEntity<RestaurantDto> getEmployeeRestaurant() {
        RestaurantDto restaurantDto = restaurantService.getEmployeeRestaurant();
        return ResponseEntity.ok(restaurantDto);
    }

    @GetMapping("/get-restaurant/{restaurantId}")
    public ResponseEntity<RestaurantDto> getRestaurantById(@PathVariable UUID restaurantId) {
        RestaurantDto restaurantDto = restaurantService.getRestaurantById(restaurantId);
        return ResponseEntity.ok(restaurantDto);
    }

    @GetMapping("/get-restaurant-tableBooking/{tableBookingId}")
    public ResponseEntity<RestaurantDto> getRestaurantByTableBookingId(@PathVariable UUID tableBookingId) {
        RestaurantDto restaurantDto = restaurantService.getRestaurantByTableBookingId(tableBookingId);
        return ResponseEntity.ok(restaurantDto);
    }
}
