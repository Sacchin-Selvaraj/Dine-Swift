package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.MenuException;
import com.dineswift.restaurant_service.mapper.MenuMapper;
import com.dineswift.restaurant_service.model.Menu;
import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.request.menu.MenuUpdateRequest;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTOWoDish;
import com.dineswift.restaurant_service.payload.response.menu.MenuResponse;
import com.dineswift.restaurant_service.repository.MenuRepository;
import com.dineswift.restaurant_service.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final AuthService authService;
    private final MenuSpecification menuSpecification;

    public void addMenu(MenuCreateRequest menuCreateRequest, UUID restaurantId) {
        log.info("Adding new menu: {}", menuCreateRequest.getMenuName());
        if (menuRepository.existsByMenuName(menuCreateRequest.getMenuName())){
            throw new MenuException("menu name already exists");
        }
        Menu menu = menuMapper.toEntity(menuCreateRequest,restaurantId);
        menu.setCreatedBy(authService.getAuthenticatedId());
        menu.setLastModifiedBy(authService.getAuthenticatedId());
        menuRepository.save(menu);
        log.info("menu added successfully: {}", menu.getMenuId());
    }

    public void deleteMenu(UUID menuId) {
        log.info("Deleting menu with ID: {}", menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId).orElseThrow(() -> new MenuException("menu not found with provided Id"));
        menu.setIsActive(false);
        menu.setLastModifiedBy(authService.getAuthenticatedId());
        menuRepository.save(menu);
        log.info("Menu deleted successfully: {}", menuId);
    }

    public void updateMenu(UUID menuId, MenuUpdateRequest menuUpdateRequest) {
        log.info("Updating menu with ID: {}", menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId).orElseThrow(() -> new MenuException("menu not found with provided Id"));

        if (menuUpdateRequest.getMenuName() != null && !menuUpdateRequest.getMenuName().isEmpty()) {
            if (!menu.getMenuName().equals(menuUpdateRequest.getMenuName()) && menuRepository.existsByMenuName(menuUpdateRequest.getMenuName())) {
                log.error("Menu name {} already exists", menuUpdateRequest.getMenuName());
                throw new MenuException("menu name already exists");
            }
            menu.setMenuName(menuUpdateRequest.getMenuName());
        }
        log.info("Updating menu description for menu ID: {}", menuId);
        if (menuUpdateRequest.getDescription() != null) {
            menu.setDescription(menuUpdateRequest.getDescription());
        }

        menu.setLastModifiedBy(authService.getAuthenticatedId());
        menuRepository.save(menu);
        log.info("menu updated successfully: {}", menuId);
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
        menu.setLastModifiedBy(authService.getAuthenticatedId());
        menuRepository.save(menu);
        log.info("Dish {} removed from menu {}", dishId, menuId);
    }

    public MenuDTO getMenuDetails(UUID menuId) {
        log.info("Fetching details for menu ID: {}", menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(() -> new MenuException("menu not found with provided Id"));
        return menuMapper.toDTO(menu);
    }

    public Page<MenuDTOWoDish> getMenusByRestaurantId(UUID restaurantId, int page, int size) {
        log.info("Fetching menus for restaurant with ID: {}", restaurantId);

        Pageable pageable = PageRequest.of(page,size);

        Specification<Menu> spec = menuSpecification.hasRestaurantId(restaurantId)
                .and(menuSpecification.isActive());

        Page<Menu> menusPage = menuRepository.findAll(spec, pageable);
        if (menusPage.isEmpty()) {
            log.warn("No menus found for restaurant with ID: {}", restaurantId);
            throw new MenuException("No menus found for restaurant with ID: " + restaurantId);
        }
        log.info("Fetched {} menus for restaurant with ID: {}", menusPage.getTotalElements(), restaurantId);
        return menusPage.map(menuMapper::toDTOWoDish);
    }
}
