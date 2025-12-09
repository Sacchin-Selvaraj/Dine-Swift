package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.model.*;
import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import com.dineswift.restaurant_service.payload.response.dish.DishImageDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTOWoDish;
import com.dineswift.restaurant_service.payload.response.menu.MenuNameResponse;
import com.dineswift.restaurant_service.repository.DishImageRepository;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuMapper {

    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final ModelMapper modelMapper;
    private final DishImageRepository dishImageRepository;


    public Menu toEntity(MenuCreateRequest menuCreateRequest, UUID restaurantId) {
        Restaurant restaurant = restaurantRepository.findByIdAndIsActive(restaurantId).orElseThrow(()-> new RestaurantException("Restaurant not found with provided Id"));

        Menu menu=new Menu();
        if (menuCreateRequest.getMenuName()!=null && !menuCreateRequest.getMenuName().isEmpty()){
            menu.setMenuName(menuCreateRequest.getMenuName());
        }
        if (menuCreateRequest.getDescription()!=null){
            menu.setDescription(menuCreateRequest.getDescription());
        }
        menu.setRestaurant(restaurant);

        List<Dish> dishes = null;
        if (!menuCreateRequest.getDishIds().isEmpty()){
           dishes = dishRepository.findAllById(menuCreateRequest.getDishIds());
        }

        if (dishes!=null){
            menu.setDishes(new HashSet<>(dishes));
        }

        return menu;
    }

    public MenuDTO toDTO(Menu menu) {

        MenuDTO menuDTO = modelMapper.map(menu, MenuDTO.class);
        List<Dish> dishes = new ArrayList<>(menu.getDishes());

        List<DishImage> images = dishImageRepository.findByDishes(dishes);

        Map<UUID, List<DishImage>> imageMap =
                images.stream().collect(Collectors.groupingBy(img -> img.getDish().getDishId()));

        Set<DishDTO> dishDTOS = dishes.stream().map(dish -> {
            DishDTO dto = modelMapper.map(dish, DishDTO.class);
            dto.setDishImages(
                    imageMap.getOrDefault(dish.getDishId(), List.of())
                            .stream()
                            .map(this::toImageDTO)
                            .toList()
            );
            return dto;
        }).collect(Collectors.toSet());

        menuDTO.setDishes(dishDTOS);
        return menuDTO;
    }

    public MenuNameResponse toMenuNameResponse(Menu menu) {
        return modelMapper.map(menu, MenuNameResponse.class);
    }

    public MenuDTOWoDish toDTOWoDish(Menu menu) {
        return modelMapper.map(menu, MenuDTOWoDish.class);
    }

    public DishImageDTO toImageDTO(DishImage dishImage) {
        return modelMapper.map(dishImage, DishImageDTO.class);
    }
}
