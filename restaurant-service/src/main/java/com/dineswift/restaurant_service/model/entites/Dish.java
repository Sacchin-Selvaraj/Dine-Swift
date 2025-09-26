package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
@Data
@RequiredArgsConstructor
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "dish_id", nullable = false, updatable = false)
    private UUID dishId;

    @NotBlank(message = "Dish name is required")
    @Size(min = 2, max = 255, message = "Dish name must be between 2 and 255 characters")
    @Column(name = "dish_name", nullable = false, length = 255)
    private String dishName;

    @NotNull(message = "Dish price is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Dish price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Dish price must have up to 8 integer digits and 2 decimal places")
    @Column(name = "dish_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal dishPrice;

    @Size(max = 1000, message = "Dish description cannot exceed 1000 characters")
    @Column(name = "dish_description", columnDefinition = "TEXT")
    private String dishDescription;

    @NotNull(message = "Available status is required")
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;

    @DecimalMin(value = "0.00", inclusive = true, message = "Discount must be greater than or equal to 0")
    @DecimalMax(value = "100.00", inclusive = true, message = "Discount cannot exceed 100")
    @Digits(integer = 3, fraction = 2, message = "Discount must have up to 3 integer digits and 2 decimal places")
    @Column(name = "discount", precision = 5, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Size(max = 500, message = "Dish comments cannot exceed 500 characters")
    @Column(name = "dish_comments", columnDefinition = "TEXT")
    private String dishComments;

    @NotNull(message = "Boolean status is required")
    @Column(name = "is_veg", nullable = false)
    private Boolean isVeg = false;

    @Size(max = 255, message = "Last modified by cannot exceed 255 characters")
    @Column(name = "last_modified_by", length = 255)
    private String lastModifiedBy;

    @PastOrPresent(message = "Last modified date must be in the past or present")
    @Column(name = "last_modified_date")
    private ZonedDateTime lastModifiedDate;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @OneToMany(mappedBy = "dish",fetch = FetchType.LAZY,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DishImage> dishImages;




}
