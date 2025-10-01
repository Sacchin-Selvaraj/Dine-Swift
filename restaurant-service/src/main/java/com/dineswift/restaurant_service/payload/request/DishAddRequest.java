package com.dineswift.restaurant_service.payload.request;


import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@RequiredArgsConstructor
public class DishAddRequest {

    @NotBlank(message = "Dish name is required")
    @Size(min = 2, max = 100, message = "Dish name must be between 2 and 100 characters")
    private String dishName;

    @NotNull(message = "Dish price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Dish price must be greater than 0")
    private BigDecimal dishPrice;

    @NotBlank(message = "Dish description is required")
    @Size(min = 10, max = 500, message = "Dish description must be between 10 and 500 characters")
    private String dishDescription;

    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;

    @DecimalMin(value = "0.0", message = "Discount cannot be negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private BigDecimal discount;

    @NotNull(message = "Vegetarian status is required")
    private Boolean isVeg;
}

