package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Employee;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.model.RestaurantImage;
import com.dineswift.restaurant_service.model.RestaurantStatus;
import com.dineswift.restaurant_service.payload.dto.RestaurantDto;
import com.dineswift.restaurant_service.payload.dto.RestaurantImageDto;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantCreateRequest;
import com.dineswift.restaurant_service.payload.request.restaurant.RestaurantUpdateRequest;
import com.dineswift.restaurant_service.repository.RestaurantImageRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class RestaurantMapper {

    private final ModelMapper mapper;
    private final RestaurantImageRepository restaurantImageRepository;


    public Restaurant toEntity(RestaurantCreateRequest restaurantCreateRequest, Employee employee) {

        Restaurant restaurant=mapper.map(restaurantCreateRequest,Restaurant.class);

        restaurant.setOwnerName(employee.getEmployeeName());
        restaurant.setRestaurantStatus(RestaurantStatus.OPEN);
        return restaurant;
    }

    public Restaurant updateEntity(Restaurant restaurant, @Valid RestaurantUpdateRequest restaurantUpdateRequest) {
        if (restaurantUpdateRequest.getRestaurantName() != null)
            restaurant.setRestaurantName(restaurantUpdateRequest.getRestaurantName());
        if (restaurantUpdateRequest.getRestaurantDescription() != null)
            restaurant.setRestaurantDescription(restaurantUpdateRequest.getRestaurantDescription());
        if (restaurantUpdateRequest.getAddress() != null)
            restaurant.setAddress(restaurantUpdateRequest.getAddress());
        if (restaurantUpdateRequest.getArea() != null)
            restaurant.setArea(restaurantUpdateRequest.getArea());
        if (restaurantUpdateRequest.getCity() != null)
            restaurant.setCity(restaurantUpdateRequest.getCity());
        if (restaurantUpdateRequest.getDistrict() != null)
            restaurant.setDistrict(restaurantUpdateRequest.getDistrict());
        if (restaurantUpdateRequest.getState() != null)
            restaurant.setState(restaurantUpdateRequest.getState());
        if (restaurantUpdateRequest.getCountry() != null)
            restaurant.setCountry(restaurantUpdateRequest.getCountry());
        if (restaurantUpdateRequest.getPincode() != null)
            restaurant.setPincode(restaurantUpdateRequest.getPincode());
        if (restaurantUpdateRequest.getWebsiteLink() != null)
            restaurant.setWebsiteLink(restaurantUpdateRequest.getWebsiteLink());
        if (restaurantUpdateRequest.getOpeningTime() != null)
            restaurant.setOpeningTime(restaurantUpdateRequest.getOpeningTime());
        if (restaurantUpdateRequest.getClosingTime() != null)
            restaurant.setClosingTime(restaurantUpdateRequest.getClosingTime());

        return restaurant;
    }

    public RestaurantDto toDTO(Restaurant restaurant) {
        RestaurantDto restaurantDTO = mapper.map(restaurant, RestaurantDto.class);

        List<RestaurantImage> restaurantImages = restaurantImageRepository.findByRestaurant(restaurant);

        if (!restaurantImages.isEmpty())
            restaurantDTO.setRestaurantImages(restaurantImages.stream().map(this::toImageDTO).toList());

        return restaurantDTO;
    }

    public RestaurantImage toImageEntity(Map<String, Object> uploadResult, Restaurant restaurant) {
        RestaurantImage restaurantImage = new RestaurantImage();
        restaurantImage.setRestaurant(restaurant);
        restaurantImage.setPublicId((String) uploadResult.get("publicId"));
        restaurantImage.setImageUrl((String) uploadResult.get("url"));
        restaurantImage.setSecureUrl((String) uploadResult.get("secureUrl"));
        restaurantImage.setFormat((String) uploadResult.get("format"));
        restaurantImage.setResourceType((String) uploadResult.get("resourceType"));
        restaurantImage.setBytes((Long) uploadResult.get("bytes"));
        restaurantImage.setWidth((Integer) uploadResult.get("width"));
        restaurantImage.setHeight((Integer) uploadResult.get("height"));
        return restaurantImage;
    }

    public RestaurantImageDto toImageDTO(RestaurantImage image) {
        return mapper.map(image, RestaurantImageDto.class);
    }

    public Page<RestaurantDto> toPageDTO(Page<Restaurant> restaurantPage) {
        List<Restaurant> restaurantList = restaurantPage.getContent();

        Map<UUID,RestaurantDto> restaurantDtoMap = getRestaurantDTOs(restaurantList);

        return restaurantPage
                .map(restaurant -> restaurantDtoMap.get(restaurant.getRestaurantId()));
    }

    private Map<UUID, RestaurantDto> getRestaurantDTOs(List<Restaurant> restaurantList) {

        List<RestaurantImage> restaurantImages = restaurantImageRepository.findByRestaurants(restaurantList);

        Map<UUID, List<RestaurantImage>> restaurantImagesMap = restaurantImages
                .stream()
                .collect(Collectors.groupingBy(
                                restaurantImage -> restaurantImage
                                                .getRestaurant()
                                                .getRestaurantId()
                        )
                );

        Map<UUID,RestaurantDto> restaurantDtoMap = restaurantList.stream()
                .map(restaurant -> toDto(restaurant,restaurantImagesMap))
                .collect(Collectors.toMap(RestaurantDto::getRestaurantId,
                        restaurantDto -> restaurantDto));

        log.info("Mapped Restaurant Dto");
        return restaurantDtoMap;

    }

    private RestaurantDto toDto(Restaurant restaurant,Map<UUID,List<RestaurantImage>> restaurantImagesMap) {
        RestaurantDto restaurantDto = mapper.map(restaurant, RestaurantDto.class);

        List<RestaurantImage> imagesForRestaurant = restaurantImagesMap
                .getOrDefault(restaurantDto.getRestaurantId(), List.of());

        restaurantDto.setRestaurantImages(imagesForRestaurant.stream()
                        .map(this::toImageDTO)
                        .toList());

        return restaurantDto;
    }
}
