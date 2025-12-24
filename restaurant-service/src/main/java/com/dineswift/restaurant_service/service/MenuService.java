package com.dineswift.restaurant_service.service;

import com.dineswift.restaurant_service.exception.MenuException;
import com.dineswift.restaurant_service.exception.RestaurantException;
import com.dineswift.restaurant_service.mapper.MenuMapper;
import com.dineswift.restaurant_service.model.Dish;
import com.dineswift.restaurant_service.model.Menu;
import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.request.menu.MenuDishAddRequest;
import com.dineswift.restaurant_service.payload.request.menu.MenuUpdateRequest;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTOWoDish;
import com.dineswift.restaurant_service.payload.response.menu.MenuResponse;
import com.dineswift.restaurant_service.repository.DishRepository;
import com.dineswift.restaurant_service.repository.MenuRepository;
import com.dineswift.restaurant_service.repository.RestaurantRepository;
import com.dineswift.restaurant_service.security.service.AuthService;
import com.dineswift.restaurant_service.specification.MenuSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final RestaurantRepository restaurantRepository;
    private final DishRepository dishRepository;
    private final MenuMapper menuMapper;
    private final AuthService authService;
    private final MenuSpecification menuSpecification;

    @CacheEvict(
            value = "restaurant-menuWoDish",
            allEntries = true
    )
    @Transactional
    public void addMenu(MenuCreateRequest menuCreateRequest, UUID restaurantId) {
        log.info("Adding new menu: {}", menuCreateRequest.getMenuName());

        if (!restaurantRepository.existsById(restaurantId)){
            log.error("Restaurant not found with ID: {}", restaurantId);
            throw new RestaurantException("restaurant not found with provided Id");
        }

        if (menuRepository.existsByMenuNameAndRestaurantId(menuCreateRequest.getMenuName(),restaurantId)){
            throw new MenuException("menu name already exists");
        }

        Menu menu = menuMapper.toEntity(menuCreateRequest,restaurantId);
        menu.setCreatedBy(authService.getAuthenticatedId());
        menu.setLastModifiedBy(authService.getAuthenticatedId());

        menuRepository.save(menu);
        log.info("menu added successfully: {}", menu.getMenuId());
    }

    @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant-menuWoDish",
                            allEntries = true
                    ),
                    @CacheEvict(
                            value = {"restaurant-menuDetails","restaurant:get-dish-menuId"},
                            key = "#menuId"
                    )
            }
    )
    @Transactional
    public void deleteMenu(UUID menuId) {
        log.info("Deleting menu with ID: {}", menuId);

        Menu menu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(() -> new MenuException("menu not found with provided Id"));

        menu.deactivate();
        menu.setLastModifiedBy(authService.getAuthenticatedId());

        menuRepository.save(menu);

        log.info("Menu deleted successfully: {}", menuId);
    }

   @Caching(
            evict = {
                    @CacheEvict(
                            value = "restaurant-menuDetails",
                            key = "#menuId"
                    ),
                    @CacheEvict(
                            value = "restaurant-menuWoDish",
                            allEntries = true
                    )
            }
   )
   @Transactional
   public void updateMenu(UUID menuId, MenuUpdateRequest menuUpdateRequest) {
        log.info("Updating menu with ID: {}", menuId);
        Menu menu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(() -> new MenuException("menu not found with provided Id"));

        UUID restaurantId = menuRepository.getRestaurantIdByMenuId(menuId);

        if (menuUpdateRequest.getMenuName() != null && !menuUpdateRequest.getMenuName().isEmpty()) {

            if (menuRepository.existsByMenuNameAndRestaurantIdWithMenuId(menuUpdateRequest.getMenuName(),menuId,restaurantId)) {
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
        log.info("menu updated successfully: {}", menuId);
        menuRepository.save(menu);
    }

    @Transactional(readOnly = true)
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

    @CacheEvict(
            value = {"restaurant-menuDetails","restaurant:get-dish-menuId"},
            key = "#menuId"
    )
    @Transactional
    public void removeDishFromMenu(UUID menuId, UUID dishId) {
        log.info("Removing dish {} from menu {}", dishId, menuId);

        Menu menu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(() -> new MenuException("menu not found with provided Id"));

        boolean removed = menu.getDishes().removeIf(dish -> dish.getDishId().equals(dishId));

        if (!removed) {
            throw new MenuException("Dish not found in the specified menu");
        }

        menu.setLastModifiedBy(authService.getAuthenticatedId());
        menuRepository.save(menu);
        log.info("Dish {} removed from menu {}", dishId, menuId);
    }


    @CacheEvict(
            value = {"restaurant-menuDetails","restaurant:get-dish-menuId"},
            key = "#menuId"
    )
    @Transactional
    public void addDishToMenu(UUID menuId, MenuDishAddRequest menuDishAddRequest) {

        Menu existingMenu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(()-> new MenuException("No Menu found with this provided Id"));

        List<Dish> newDishes = dishRepository.findAllById(menuDishAddRequest.getDishIds());

        boolean changed = existingMenu.getDishes().addAll(newDishes);

        if (changed) {
            log.info("Successfully added {} new dishes to menu {}", newDishes.size(), menuId);
        }
    }


    @Cacheable(
            value = "restaurant-menuDetails",
            key = "#menuId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public MenuDTO getMenuDetails(UUID menuId) {
        log.info("Fetching details for menu ID: {}", menuId);

        Menu menu = menuRepository.findByIdAndIsActive(menuId)
                .orElseThrow(() -> new MenuException("Menu not found with provided Id"));
        log.info("Fetched details for menu ID: {}", menuId);

        return menuMapper.toDTO(menu);
    }

    @Cacheable(
            value = "restaurant-menuWoDish",
            key = "#restaurantId + '-' + #page + '-' + #size",
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public CustomPageDto<MenuDTOWoDish> getMenusByRestaurantId(UUID restaurantId, int page, int size) {
        log.info("Fetching menus for restaurant with ID: {}", restaurantId);

        Pageable pageable = PageRequest.of(page,size);

        Specification<Menu> spec = menuSpecification.hasRestaurantId(restaurantId)
                .and(menuSpecification.isActive());

        Page<Menu> menusPage = menuRepository.findAll(spec, pageable);

        if (menusPage.isEmpty()) {
            log.warn("No menus found for restaurant with ID: {}", restaurantId);
            return new CustomPageDto<>(Page.empty());
        }
        log.info("Fetched {} menus for restaurant with ID: {}", menusPage.getTotalElements(), restaurantId);

        return new CustomPageDto<>(menusPage.map(menuMapper::toDTOWoDish));
    }

}
