package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"restaurant_name", "city", "state"}),
        @UniqueConstraint(columnNames = "contact_number"),
        @UniqueConstraint(columnNames = "contact_email")
})
@Data
@RequiredArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "restaurant_id", nullable = false, updatable = false)
    private UUID restaurantId;

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 2, max = 255, message = "Restaurant name must be between 2 and 255 characters")
    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "restaurant_description")
    private String restaurantDescription;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 500, message = "Address must be between 5 and 500 characters")
    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Size(max = 100, message = "Area cannot exceed 100 characters")
    @Column(name = "area")
    private String area;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "city", nullable = false)
    private String city;

    @Size(max = 100, message = "District cannot exceed 100 characters")
    @Column(name = "district")
    private String district;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    @Column(name = "state", nullable = false)
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Column(name = "country", nullable = false)
    private String country = "India";

    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    @Column(name = "pincode")
    private String pincode;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 8, message = "Latitude must have 2 integer and 8 decimal places")
    @Column(name = "latitude", precision = 10, scale = 8)
    private Double latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 8, message = "Longitude must have 3 integer and 8 decimal places")
    @Column(name = "longitude", precision = 11, scale = 8)
    private Double longitude;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{10,20}$",
            message = "Contact number must be valid and between 10-20 digits")
    @Column(name = "contact_number", nullable = false)
    private String contactNumber;

    @Email(message = "Contact email must be valid")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    @Column(name = "contact_email")
    private String contactEmail;

    @Size(max = 500, message = "Website link cannot exceed 500 characters")
    @Column(name = "website_link")
    private String websiteLink;

    @NotBlank(message = "Owner name is required")
    @Size(max = 255, message = "Owner name cannot exceed 255 characters")
    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @NotNull(message = "Restaurant status is required")
    @Column(name = "restaurant_status")
    private RestaurantStatus restaurantStatus;

    @NotNull(message = "Opening time is required")
    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @NotNull(message = "Closing time is required")
    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified_date", nullable = false)
    private ZonedDateTime lastModifiedDate;

    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

}