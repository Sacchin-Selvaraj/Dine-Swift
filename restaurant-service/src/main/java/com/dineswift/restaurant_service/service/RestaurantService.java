package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.exception.ImageException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.geocoding.service.GeocodingService;
import com.dineswift.restaurant_service.mapper.RestaurantMapper;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.payload.dto.RestaurantDto;
import com.dineswift.restaurant_service.payload.dto.RestaurantImageDto;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.RestaurantImageRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.repository.TableBookingRepository;
import com.dineswift.restaurant_service.security.service.AuthService;
import com.dineswift.restaurant_service.service.records.RestaurantFilter;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final TableBookingRepository tableBookingRepository;
    private final RestaurantMapper restaurantMapper;
    private final ImageService imageService;
    private final RestaurantImageRepository restaurantImageRepository;
    private final GeocodingService geocodingService;
    private final AuthService authService;
    private final CacheManager cacheManager;

    @CacheEvict(
            value = "restaurant:getRestaurants",
            allEntries = true
    )
    public void createRestaurant(RestaurantCreateRequest restaurantCreateRequest) {
        UUID employeeId = authService.getAuthenticatedId();
        if (restaurantCreateRequest==null || employeeId==null) {
            log.error("Invalid Restaurant Create data");
            throw new RestaurantException("Invalid Restaurant Create data");
        }
        Employee employee=employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found with id: " + employeeId));

        if (employee.getRestaurant()!=null && employee.getRestaurant().getIsActive()){
            log.error("Admin Can have only one Active Restaurant");
            throw new RestaurantException("Admin Can have only one Active Restaurant");
        }
        Restaurant restaurant=restaurantMapper.toEntity(restaurantCreateRequest,employee);
        restaurant.setLastModifiedBy(authService.getAuthenticatedId());

        String fullAddress=String.format("%s, %s, %s, %s", restaurantCreateRequest.getAddress(),
                restaurantCreateRequest.getCity(),
                restaurantCreateRequest.getState(),
                restaurantCreateRequest.getCountry());
        Coordinates coordinates=geocodingService.getCoordinates(fullAddress);
        restaurant.setLatitude(coordinates.getLatitude());
        restaurant.setLongitude(coordinates.getLongitude());

        employee.setRestaurant(restaurant);
        log.info("Setting Authenticated Employee as last modified by for Restaurant");
        employee.setLastModifiedBy(authService.getAuthenticatedId());
        employeeRepository.save(employee);
    }

    @Cacheable(
            value = "restaurant:getRestaurants",
            key = "#filter.hashCode()",
            unless = "#result == null or #result.isEmpty()"
    )
    public CustomPageDto<RestaurantDto> getRestaurants(RestaurantFilter filter) {
        try {
            RestaurantStatus restaurantStatusEnum=null;
            String restaurantStatus=filter.restaurantStatus();
            log.info("Creating sort object for restaurants");
            Sort sort = filter.sortDir().equalsIgnoreCase("asc") ? Sort.by(filter.sortBy()).ascending() : Sort.by(filter.sortBy()).descending();
            if (filter.restaurantStatus()==null) restaurantStatus="OPEN";
            restaurantStatusEnum=RestaurantStatus.fromDisplayName(restaurantStatus);

            log.info("Building restaurant specifications for filtering");
            Specification<Restaurant> spec = Specification.<Restaurant>allOf()
                    .and(RestaurantSpecification.hasId(filter.restaurantId()))
                    .and(RestaurantSpecification.hasStatus(restaurantStatusEnum))
                    .and(RestaurantSpecification.hasArea(filter.area()))
                    .and(RestaurantSpecification.hasCity(filter.city()))
                    .and(RestaurantSpecification.hasDistrict(filter.district()))
                    .and(RestaurantSpecification.hasState(filter.state()))
                    .and(RestaurantSpecification.hasCountry(filter.country()))
                    .and(RestaurantSpecification.nameContains(filter.restaurantName()))
                    .and(RestaurantSpecification.openBefore(filter.openingTime()))
                    .and(RestaurantSpecification.closeAfter(filter.closingTime()));

            Pageable pageable = PageRequest.of(filter.page(), filter.size(), sort);

            Page<Restaurant> restaurantPage;
            log.info("Retrieving restaurants from repository with specifications and pagination");
            restaurantPage=restaurantRepository.findAll(spec, pageable);
            if (restaurantPage.hasContent()) {
                return new CustomPageDto<>(restaurantPage.map(restaurantMapper::toDTO));
            }else {
                return new CustomPageDto<>(Page.empty());
            }
        } catch (Exception e) {
            log.error("Error retrieving restaurants: {}", e.getMessage());
            throw new RestaurantException("Restaurant retrieval failed: " + e.getMessage());
        }
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:getEmployeeRestaurant",
                            key = "@authService.getAuthenticatedId()"
                    ),
                    @CacheEvict(
                            value = "restaurant:getRestaurantById",
                            key = "#restaurantId"
                    ),
                    @CacheEvict(
                            value = "restaurant:getRestaurants",
                            allEntries = true
                    )
            }
    )
    public void editRestaurantDetails(UUID restaurantId, @Valid RestaurantUpdateRequest restaurantUpdateRequest) {
        if (restaurantId == null || restaurantUpdateRequest == null) {
            throw new RestaurantException("Invalid Restaurant Update data");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        log.info("Updating restaurant details for restaurant id: {}", restaurantId);
        restaurant=restaurantMapper.updateEntity(restaurant, restaurantUpdateRequest);
        restaurantRepository.save(restaurant);
        restaurant.setLastModifiedBy(authService.getAuthenticatedId());
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:getEmployeeRestaurant",
                            key = "@authService.getAuthenticatedId()"
                    ),
                    @CacheEvict(
                            value = "restaurant:getRestaurantById",
                            key = "#restaurantId"
                    ),
                    @CacheEvict(
                            value = "restaurant:getRestaurants",
                            allEntries = true
                    )
            }
    )
    public void deactivateRestaurant(UUID restaurantId) {
        if (restaurantId == null) {
            throw new RestaurantException("Invalid Restaurant Id");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        log.info("Deactivating restaurant with id: {}", restaurantId);
        restaurant.setIsActive(false);
        restaurant.setLastModifiedBy(authService.getAuthenticatedId());
        restaurantRepository.save(restaurant);
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant:getEmployeeRestaurant",
                            key = "@authService.getAuthenticatedId()"
                    ),
                    @CacheEvict(
                            value = "restaurant:getRestaurantById",
                            key = "#restaurantId"
                    ),
                    @CacheEvict(
                            value = "restaurant:getRestaurants",
                            allEntries = true
                    )
            }
    )
    public void changeRestaurantStatus(UUID restaurantId, String status) {
        if (restaurantId == null || status == null) {
            throw new RestaurantException("Invalid data for changing Restaurant status");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId+" or is inactive"));
        try {
            RestaurantStatus restaurantStatus = RestaurantStatus.fromDisplayName(status);
            restaurant.setRestaurantStatus(restaurantStatus);
            restaurant.setLastModifiedBy(authService.getAuthenticatedId());
            restaurantRepository.save(restaurant);
        } catch (IllegalArgumentException e) {
            log.error("Invalid Restaurant status: {}", status);
            throw new RestaurantException("Invalid status: " + status);
        }
    }

    public CompletableFuture<Void> uploadRestaurantImage(UUID restaurantId, MultipartFile imageFile) throws ExecutionException, InterruptedException {
        if (restaurantId == null || imageFile == null || imageFile.isEmpty()) {
            log.error("Invalid data for uploading Restaurant image");
            throw new RestaurantException("Invalid data for uploading Restaurant image");
        }

       return imageService.uploadImage(imageFile,"restaurant").thenAcceptAsync( result-> {
           if (result != null && (Boolean) result.get("isSuccessful")) {
               evictRestaurantCaches(restaurantId);
               log.info("Image uploaded successfully for restaurant id: {}", restaurantId);
               saveRestaurantImage(result, restaurantId);
           } else {
               log.error("Image upload failed for restaurant id: {}. Error: {}", restaurantId, result.get("error"));
               throw new RestaurantException("Image upload failed");
           }

       }).exceptionally(ex -> {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new CompletionException(new RestaurantException("Image upload failed" + cause));
        });

    }

    @Transactional
    public void saveRestaurantImage(Map<String, Object> result, UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));

        RestaurantImage imageEntity = restaurantMapper.toImageEntity(result, restaurant);
        restaurantImageRepository.save(imageEntity);
    }

    public CompletableFuture<Void> deleteRestaurantImage(UUID imageId) {
        if (imageId == null) {
            throw new RestaurantException("Invalid Image Id");
        }
        RestaurantImage restaurantImage = restaurantImageRepository.findById(imageId)
                .orElseThrow(() -> new ImageException("Restaurant Image not found with id: " + imageId));

        return imageService.deleteImage(restaurantImage.getPublicId()).thenAcceptAsync(
                result->{
                    evictRestaurantCaches(restaurantImage.getRestaurant().getRestaurantId());
                    log.info("Image deletion Successful for image id: {}", imageId);
                    restaurantImageRepository.delete(restaurantImage);
                }
        ).exceptionally(throwable -> {
            log.error("Image deletion failed for image id: {}. Error: {}", imageId, throwable.getMessage());
            throw new CompletionException(new ImageException("Image deletion failed: " + throwable.getMessage()));
        });
    }

    public void evictRestaurantCaches(UUID restaurantId) {
        log.info("Evicting caches related to restaurant id: {}", restaurantId);
        Objects.requireNonNull(cacheManager.getCache("restaurant:getRestaurantById")).evict(restaurantId);
        Objects.requireNonNull(cacheManager.getCache("restaurant:getEmployeeRestaurant")).evict(authService.getAuthenticatedId());
        Objects.requireNonNull(cacheManager.getCache("restaurant:getRestaurants")).clear();
    }

    public List<RestaurantImageDto> getRestaurantImages(UUID restaurantId) {
        if (restaurantId == null) {
            throw new RestaurantException("Invalid Restaurant Id");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        List<RestaurantImage> restaurantImages = restaurantImageRepository.findByRestaurant(restaurant);

        return restaurantImages.stream().map(restaurantMapper::toImageDTO).toList();
    }

    @Cacheable(
            value = "restaurant:getEmployeeRestaurant",
            key = "@authService.getAuthenticatedId()",
            unless = "#result == null"
    )
    public RestaurantDto getEmployeeRestaurant() {
        log.info("Fetching restaurant for authenticated employee");
        UUID employeeId = authService.getAuthenticatedId();
        if (employeeId == null) {
            log.error("Authenticated Employee Id not found");
            throw new EmployeeException("Authenticated Employee Id not found");
        }
        Employee loggedInEmployee = employeeRepository.findByIdAndIsActive(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found with id: " + employeeId));
        Restaurant restaurant = loggedInEmployee.getRestaurant();
        if (restaurant == null || !restaurant.getIsActive()) {
            log.error("No active restaurant associated with employee id: {}", employeeId);
            throw new RestaurantException("No active restaurant associated with the employee");
        }
        return restaurantMapper.toDTO(restaurant);
    }

    @Cacheable(
            value = "restaurant:getRestaurantById",
            key = "#restaurantId",
            unless = "#result == null"
    )
    public RestaurantDto getRestaurantById(UUID restaurantId) {
        log.info("Fetching restaurant by id: {}", restaurantId);
        if (restaurantId == null) {
            throw new RestaurantException("Invalid Restaurant Id");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        return restaurantMapper.toDTO(restaurant);
    }

    public RestaurantDto getRestaurantByTableBookingId(UUID tableBookingId) {
        log.info("Fetching restaurant by table booking id: {}", tableBookingId);
        if (tableBookingId == null) {
            throw new RestaurantException("Invalid Table Booking Id");
        }
        TableBooking existingBooking = tableBookingRepository.findById(tableBookingId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found for table booking id: " + tableBookingId));
        return restaurantMapper.toDTO(existingBooking.getRestaurant());
    }
}
