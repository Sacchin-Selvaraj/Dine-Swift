package com.dineswift.restaurant_service.controller;

import com.dineswift.restaurant_service.payload.request.menu.MenuCreateRequest;
import com.dineswift.restaurant_service.payload.request.menu.MenuUpdateRequest;
import com.dineswift.restaurant_service.payload.response.MessageResponse;
import com.dineswift.restaurant_service.payload.response.menu.MenuDTO;
import com.dineswift.restaurant_service.payload.response.menu.MenuResponse;
import com.dineswift.restaurant_service.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/add-menu/{restaurantId}")
    public ResponseEntity<MessageResponse> addMenu(@RequestBody MenuCreateRequest menuCreateRequest, @PathVariable UUID restaurantId) {
        log.info("Received add menu request");
        menuService.addMenu(menuCreateRequest,restaurantId);
        return new ResponseEntity<>(new MessageResponse("menu created successfully"), HttpStatus.CREATED);
    }

    @DeleteMapping("/delete-menu/{menuId}")
    public ResponseEntity<Void> deleteMenu(@PathVariable UUID menuId) {
        log.info("Received delete menu request for menuId: {}", menuId);
        menuService.deleteMenu(menuId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PatchMapping("/update-menu/{menuId}")
    public ResponseEntity<MenuDTO> updateMenu(@PathVariable UUID menuId, @RequestBody MenuUpdateRequest menuUpdateRequest) {
        MenuDTO menuDTO = menuService.updateMenu(menuId, menuUpdateRequest);
        return ResponseEntity.ok(menuDTO);
    }

    @GetMapping("/get-menu/{restaurantId}")
    public ResponseEntity<MenuResponse> getMenuByRestaurantId(@PathVariable UUID restaurantId) {
        MenuResponse menuResponse = menuService.getMenuByRestaurantId(restaurantId);
        return ResponseEntity.ok(menuResponse);
    }

    @DeleteMapping("/remove-dish/{menuId}/{dishId}")
    public ResponseEntity<Void> removeDishFromMenu(@PathVariable UUID menuId, @PathVariable UUID dishId) {
        log.info("Received request to remove dish {} from menu {}", dishId, menuId);
        menuService.removeDishFromMenu(menuId, dishId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
