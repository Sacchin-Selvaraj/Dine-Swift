package com.dineswift.restaurant_service.mapper;

import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.model.Category;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.Menu;
import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuNameResponse;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MenuMapper {

    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;
    private final DishMapper dishMapper;
    private final ModelMapper modelMapper;


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
        if (menu.getDishes() != null && !menu.getDishes().isEmpty()) {
            menuDTO.setDishes(menu.getDishes().stream().map(dishMapper::toDTO).collect(Collectors.toSet()));
        }
        return menuDTO;
    }

    public MenuNameResponse toMenuNameResponse(Menu menu) {
        return modelMapper.map(menu, MenuNameResponse.class);
    }
}
