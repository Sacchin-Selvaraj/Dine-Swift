package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.MenuException;
import com.dineswift.restaurant_service.mapper.MenuMapper;
import com.dineswift.restaurant_service.model.Menu;
import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.request.menu.MenuUpdateRequest;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuResponse;
import com.dineswift.restaurant_service.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    public void addMenu(MenuCreateRequest menuCreateRequest, UUID restaurantId) {
        log.info("Adding new menu: {}", menuCreateRequest.getMenuName());
        if (menuRepository.existsByMenuName(menuCreateRequest.getMenuName())){
            throw new MenuException("menu name already exists");
        }
        Menu menu = menuMapper.toEntity(menuCreateRequest,restaurantId);
        menu.setCreatedBy(restaurantId);
        // After working with authentication, set the createdBy field to the authenticated user's ID
        menuRepository.save(menu);
        log.info("menu added successfully: {}", menu.getMenuId());
    }

    public void deleteMenu(UUID menuId) {
        log.info("Deleting menu with ID: {}", menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId).orElseThrow(() -> new MenuException("menu not found with provided Id"));
        menu.setIsActive(false);
        menuRepository.save(menu);
        log.info("Menu deleted successfully: {}", menuId);
    }

    public MenuDTO updateMenu(UUID menuId, MenuUpdateRequest menuUpdateRequest) {
        log.info("Updating menu with ID: {}", menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId).orElseThrow(() -> new MenuException("menu not found with provided Id"));

        if (menuUpdateRequest.getMenuName() != null && !menuUpdateRequest.getMenuName().isEmpty()) {
            if (!menu.getMenuName().equals(menuUpdateRequest.getMenuName()) && menuRepository.existsByMenuName(menuUpdateRequest.getMenuName())) {
                throw new MenuException("menu name already exists");
            }
            menu.setMenuName(menuUpdateRequest.getMenuName());
        }

        if (menuUpdateRequest.getDescription() != null) {
            menu.setDescription(menuUpdateRequest.getDescription());
        }

        menuRepository.save(menu);
        log.info("menu updated successfully: {}", menuId);
        return menuMapper.toDTO(menu);
    }

    public MenuResponse getMenuByRestaurantId(UUID restaurantId) {
        log.info("Fetching menu for restaurant ID: {}", restaurantId);
        MenuResponse menuResponse = new MenuResponse();
        List<Menu> menuList = menuRepository.findAllByRestaurant_RestaurantIdAndIsActive(restaurantId, true);
        if (menuList.isEmpty()) {
            log.warn("No menus found for restaurant ID: {}", restaurantId);
            return menuResponse;
        }
        menuResponse.setMenuNameResponses(menuList.stream().map(menuMapper::toMenuNameResponse).toList());
        log.info("Fetched {} menus for restaurant ID: {}", menuResponse.getMenuNameResponses().size(), restaurantId);
        return menuResponse;
    }

    public void removeDishFromMenu(UUID menuId, UUID dishId) {
        log.info("Removing dish {} from menu {}", dishId, menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId).orElseThrow(() -> new MenuException("menu not found with provided Id"));
        boolean removed = menu.getDishes().removeIf(dish -> dish.getDishId().equals(dishId));
        if (!removed) {
            throw new MenuException("Dish not found in the specified menu");
        }
        menuRepository.save(menu);
        log.info("Dish {} removed from menu {}", dishId, menuId);
    }

    public MenuDTO getMenuDetails(UUID menuId) {
        Menu menu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(() -> new MenuException("menu not found with provided Id"));
        return menuMapper.toDTO(menu);
    }
}
