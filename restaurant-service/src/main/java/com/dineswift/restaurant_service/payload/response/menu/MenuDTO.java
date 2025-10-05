package com.dineswift.restaurant_service.payload.response.menu;

import com.dineswift.restaurant_service.model.Restaurant;
import com.dineswift.restaurant_service.payload.response.dish.DishDTO;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MenuDTO {

    private UUID menuId;
    private String menuName;
    private String description;
    private UUID lastModifiedBy;
    private ZonedDateTime lastModifiedDate;
    private Set<DishDTO> dishes;
}
