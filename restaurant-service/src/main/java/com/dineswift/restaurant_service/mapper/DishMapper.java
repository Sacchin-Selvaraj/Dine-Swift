package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.DishImage;
import com.dineswift.restaurant_service.payload.request.dish.DishAddRequest;
import com.dineswift.restaurant_service.payload.request.dish.DishUpdateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.payload.response.dish.DishImageDTO;
import com.dineswift.restaurant_service.repository.DishImageRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DishMapper {

    private final ModelMapper mapper;
    private final DishImageRepository dishImageRepository;


    public Dish toEntity(DishAddRequest dishAddRequest) {
        Dish dish = mapper.map(dishAddRequest, Dish.class);
        dish.setDishStarRating(BigDecimal.valueOf(0.0));
        dish.setDishTotalRating(0.0);
        dish.setDishTotalRatingCount(0);
        return dish;
    }

    public Dish toEntity(DishUpdateRequest dishUpdateRequest, Dish dish) {
        if (dishUpdateRequest.getDishName()!=null){
            dish.setDishName(dishUpdateRequest.getDishName());
        }
        if (dishUpdateRequest.getDishPrice()!=null){
            dish.setDishPrice(dishUpdateRequest.getDishPrice());
        }
        if (dishUpdateRequest.getDishDescription()!=null){
            dish.setDishDescription(dishUpdateRequest.getDishDescription());
        }
        if (dishUpdateRequest.getIsAvailable()!=null){
            dish.setIsAvailable(dishUpdateRequest.getIsAvailable());
        }
        if (dishUpdateRequest.getDiscount()!=null){
            dish.setDiscount(dishUpdateRequest.getDiscount());
        }
        if (dishUpdateRequest.getDishComments()!=null){
            dish.setDishComments(dishUpdateRequest.getDishComments());
        }
        if (dishUpdateRequest.getIsVeg()!=null){
            dish.setIsVeg(dishUpdateRequest.getIsVeg());
        }
        if (dishUpdateRequest.getIsActive()!=null){
            dish.setIsActive(dishUpdateRequest.getIsActive());
        }
        return dish;
    }

    public DishDTO toDTO(Dish updatedDish) {
        List<DishImage> dishImages = dishImageRepository.findByDish(updatedDish);
        DishDTO dishDTO = mapper.map(updatedDish, DishDTO.class);
        dishDTO.setDishImages(dishImages.stream().map(this::toImageDTO).toList());
        return dishDTO;
    }

    public DishImage toImageEntity(Map<String, Object> uploadResult, Dish dish) {
        DishImage dishImage = new DishImage();
        dishImage.setPublicId((String) uploadResult.get("publicId"));
        dishImage.setImageUrl((String) uploadResult.get("url"));
        dishImage.setSecureUrl((String) uploadResult.get("secureUrl"));
        dishImage.setFormat((String) uploadResult.get("format"));
        dishImage.setResourceType((String) uploadResult.get("resourceType"));
        dishImage.setBytes((Long) uploadResult.get("bytes"));
        dishImage.setWidth((Integer) uploadResult.get("width"));
        dishImage.setHeight((Integer) uploadResult.get("height"));
        dishImage.setDish(dish);
        return dishImage;
    }

    public DishImageDTO toImageDTO(DishImage dishImage) {
        return mapper.map(dishImage, DishImageDTO.class);
    }
}
