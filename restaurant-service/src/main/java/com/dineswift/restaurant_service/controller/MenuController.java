package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.request.menu.MenuUpdateRequest;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTOWoDish;
import com.dineswift.restaurant_service.payload.response.menu.MenuResponse;
import com.dineswift.restaurant_service.service.CustomPageDto;
import com.dineswift.restaurant_service.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/restaurant/menu")
public class MenuController {

    private final MenuService menuService;

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PostMapping("/add-menu/{restaurantId}")
    public ResponseEntity<Void> addMenu(@RequestBody MenuCreateRequest menuCreateRequest,
                                                   @PathVariable UUID restaurantId) {
        log.info("Received add menu request");
        menuService.addMenu(menuCreateRequest,restaurantId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/delete-menu/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID menuId) {
        log.info("Received delete menu request for menuId: {}", menuId);
        menuService.deleteMenu(menuId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @PatchMapping("/update-menu/{menuId}")
    public ResponseEntity<Void> updateMenu(@PathVariable UUID menuId,
                                           @RequestBody MenuUpdateRequest menuUpdateRequest) {
        menuService.updateMenu(menuId, menuUpdateRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get-menu/{restaurantId}")
    public ResponseEntity<MenuResponse> getMenuByRestaurantId(@PathVariable UUID restaurantId) {
        MenuResponse menuResponse = menuService.getMenuByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuResponse);
    }

    @PreAuthorize(("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')"))
    @DeleteMapping("/remove-dish/{menuId}/{dishId}")
    public ResponseEntity<Void> removeDishFromMenu(@PathVariable UUID menuId, @PathVariable UUID dishId) {
        log.info("Received request to remove dish {} from menu {}", dishId, menuId);
        menuService.removeDishFromMenu(menuId, dishId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-menu-details/{menuId}")
    public ResponseEntity<MenuDTO> getMenuDetails(@PathVariable UUID menuId) {
        MenuDTO menuDTO = menuService.getMenuDetails(menuId);
        return ResponseEntity.ok(menuDTO);
    }

    @GetMapping("/get-menus/{restaurantId}")
    public ResponseEntity<CustomPageDto<MenuDTOWoDish>> getMenusByRestaurantId(@PathVariable UUID restaurantId,
                                                                               @RequestParam(name = "page", defaultValue = "0") int page,
                                                                               @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Received request to get menus for restaurant with ID: {}", restaurantId);
        CustomPageDto<MenuDTOWoDish> menus = menuService.getMenusByRestaurantId(restaurantId, page, size);
        return ResponseEntity.ok().body(menus);
    }
}
