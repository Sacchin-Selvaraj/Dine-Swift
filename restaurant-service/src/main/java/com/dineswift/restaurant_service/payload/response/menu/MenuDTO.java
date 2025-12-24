package com.dineswift.restaurant_service.payload.response.menu;

import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MenuDTO {
    private UUID menuId;
    private String menuName;
    private String description;
    private Set<DishDTO> dishes;
}
