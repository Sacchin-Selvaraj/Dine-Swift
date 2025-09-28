package com.dineswift.restaurant_service.payload.request.restaurant;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Data
@RequiredArgsConstructor
@Builder
public class RestaurantUpdateRequest {


    private String restaurantName;

    private String restaurantDescription;

    private String address;

    private String area;

    private String city;

    private String district;

    private String state;

    private String country;

    private String pincode;

    private String websiteLink;

    private LocalTime openingTime;

    private LocalTime closingTime;
}
