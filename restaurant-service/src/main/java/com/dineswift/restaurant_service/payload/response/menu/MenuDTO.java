package com.dineswift.restaurant_service.payload.response.menu;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MenuDTO {

    private UUID categoryId;
    private String categoryName;
    private String description;
    private UUID lastModifiedBy;
    private Instant lastModifiedDate;
    private Set<DishDTO> dishes;
}
