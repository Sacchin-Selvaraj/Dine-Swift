package com.dineswift.restaurant_service.payload.response.dish;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class DishDTO {

    private UUID dishId;
    private String dishName;
    private BigDecimal dishPrice;
    private String dishDescription;
    private Boolean isAvailable;
    private BigDecimal discount;
    private BigDecimal dishStarRating;
    private String dishComments;
    private Boolean isVeg;
    private Boolean isActive;
    private List<DishImageDTO> dishImages;

}
