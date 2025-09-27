package com.dineswift.restaurant_service.payload.request.restaurant;

import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalTime;

@Data
@RequiredArgsConstructor
public class RestaurantCreateRequest {

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 255, message = "Restaurant name must be between 2 and 255 characters")
    private String restaurantName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String restaurantDescription;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    private String address;

    @Size(max = 100, message = "Area cannot exceed 100 characters")
    private String area;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Pattern(regexp = "^[0-9]{6}$", message = "PinCode must be 6 digits")
    private String pincode;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{10,20}$",
            message = "Contact number must be valid and between 10-20 digits")
    private String contactNumber;

    @Email(message = "Contact email must be valid")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    private String contactEmail;

    @Size(max = 500, message = "Website link cannot exceed 500 characters")
    private String websiteLink;

    @NotNull(message = "Opening time is required")
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

}
