package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.EmployeeException;
import com.dineswift.restaurant_service.exception.ImageException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.RestaurantMapper;
import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantImage;
import com.dineswift.restaurant_service.model.RestaurantStatus;
import com.dineswift.restaurant_service.payload.dto.RestaurantDTO;
import com.dineswift.restaurant_service.payload.dto.RestaurantImageDTO;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import com.dineswift.restaurant_service.repository.EmployeeRepository;
import com.dineswift.restaurant_service.repository.RestaurantImageRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final RestaurantMapper restaurantMapper;
    private final ImageService imageService;
    private final RestaurantImageRepository restaurantImageRepository;

    public void createRestaurant(RestaurantCreateRequest restaurantCreateRequest, UUID employeeId) {
        if (restaurantCreateRequest==null || employeeId==null) {
            throw new RestaurantException("Invalid Restaurant Create data");
        }
        Employee employee=employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeException("Employee not found with id: " + employeeId));

        if (employee.getRestaurant()!=null && employee.getRestaurant().getIsActive()){
            throw new RestaurantException("Admin Can have only one Active Restaurant");
        }
        Restaurant restaurant=restaurantMapper.toEntity(restaurantCreateRequest,employee);

        // need to set latitude and longitude from address using geocoding service in future
        employee.setRestaurant(restaurant);

        employeeRepository.save(employee);
    }

    public Page<RestaurantDTO> getRestaurants(int page, int size, String restaurantStatus, String sortBy,
                                              String area, String city, String district, String state, String country,
                                              String restaurantName, LocalTime openingTime, LocalTime closingTime) {
        RestaurantStatus restaurantStatusEnum=null;

        Sort sort = sortBy.equalsIgnoreCase("asc") ? Sort.by("restaurantName").ascending() : Sort.by("restaurantName").descending();

        restaurantStatusEnum=RestaurantStatus.fromDisplayName(restaurantStatus);

        Specification<Restaurant> spec = Specification.<Restaurant>allOf()
                .and(RestaurantSpecification.hasStatus(restaurantStatusEnum))
                .and(RestaurantSpecification.hasArea(area))
                .and(RestaurantSpecification.hasCity(city))
                .and(RestaurantSpecification.hasDistrict(district))
                .and(RestaurantSpecification.hasState(state))
                .and(RestaurantSpecification.hasCountry(country))
                .and(RestaurantSpecification.nameContains(restaurantName))
                .and(RestaurantSpecification.openBefore(openingTime))
                .and(RestaurantSpecification.closeAfter(closingTime));

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Restaurant> restaurantPage;

        restaurantPage=restaurantRepository.findAll(spec, pageable);
        if (restaurantPage.hasContent()) {
            return restaurantPage.map(restaurantMapper::toDTO);
        }else {
            return Page.empty();
        }
    }

    public RestaurantDTO editRestaurantDetails(UUID restaurantId, @Valid RestaurantUpdateRequest restaurantUpdateRequest) {
        if (restaurantId == null || restaurantUpdateRequest == null) {
            throw new RestaurantException("Invalid Restaurant Update data");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        restaurant=restaurantMapper.updateEntity(restaurant, restaurantUpdateRequest);
        restaurantRepository.save(restaurant);
        return restaurantMapper.toDTO(restaurant);

    }

    public void deactivateRestaurant(UUID restaurantId) {
        if (restaurantId == null) {
            throw new RestaurantException("Invalid Restaurant Id");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        restaurant.setIsActive(false);
        restaurantRepository.save(restaurant);
    }


    public void changeRestaurantStatus(UUID restaurantId, String status) {
        if (restaurantId == null || status == null) {
            throw new RestaurantException("Invalid data for changing Restaurant status");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        try {
            RestaurantStatus restaurantStatus = RestaurantStatus.fromDisplayName(status);
            restaurant.setRestaurantStatus(restaurantStatus);
            restaurantRepository.save(restaurant);
        } catch (IllegalArgumentException e) {
            throw new RestaurantException("Invalid status: " + status);
        }
    }

    public void uploadRestaurantImage(UUID restaurantId, MultipartFile imageFile) {
        if (restaurantId == null || imageFile == null || imageFile.isEmpty()) {
            throw new RestaurantException("Invalid data for uploading Restaurant image");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));

        Map<String, Object> uploadResult = imageService.uploadImage(imageFile);
        if (uploadResult.get("isSuccessful") != null && (Boolean) uploadResult.get("isSuccessful")) {
            restaurantImageRepository.save(restaurantMapper.toImageEntity(uploadResult, restaurant));
        } else {
            throw new RestaurantException("Image upload failed");
        }
    }

    public void deleteRestaurantImage(UUID imageId) {
        if (imageId == null) {
            throw new RestaurantException("Invalid Image Id");
        }
        RestaurantImage restaurantImage = restaurantImageRepository.findById(imageId)
                .orElseThrow(() -> new ImageException("Restaurant Image not found with id: " + imageId));
        imageService.deleteImage(restaurantImage.getPublicId());
        restaurantImageRepository.delete(restaurantImage);
    }

    public List<RestaurantImageDTO> getRestaurantImages(UUID restaurantId) {
        if (restaurantId == null) {
            throw new RestaurantException("Invalid Restaurant Id");
        }
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId)
                .orElseThrow(() -> new RestaurantException("Restaurant not found with id: " + restaurantId));
        List<RestaurantImage> restaurantImages = restaurantImageRepository.findByRestaurant(restaurant);

        return restaurantImages.stream().map(restaurantMapper::toImageDTO).toList();
    }

}
