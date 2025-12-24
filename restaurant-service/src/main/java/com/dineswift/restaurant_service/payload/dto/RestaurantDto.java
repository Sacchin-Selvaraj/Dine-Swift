package com.dineswift.restaurant_service.payload.dto;

import com.dineswift.restaurant_service.model.RestaurantStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RestaurantDto {

    private UUID restaurantId;
    @NotBlank(message = "Restaurant name is required")
    private String restaurantName;
    private String restaurantDescription;
    private String address;
    private String area;
    private String city;
    private String district;
    private String state;
    private String country;
    private String pincode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String contactNumber;
    private String contactEmail;
    private String websiteLink;
    private String ownerName;
    private RestaurantStatus restaurantStatus;
    private LocalTime openingTime;
    private LocalTime closingTime;
    private Boolean isActive;
    private ZonedDateTime createdAt;
    private ZonedDateTime lastModifiedDate;
    private UUID lastModifiedBy;
    private List<RestaurantImageDto> restaurantImages;
}
