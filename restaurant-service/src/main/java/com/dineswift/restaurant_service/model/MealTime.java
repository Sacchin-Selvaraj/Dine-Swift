package com.dineswift.restaurant_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "meal_time")
@Data
@RequiredArgsConstructor
public class MealTime {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "meal_time_id", nullable = false, updatable = false)
    private UUID mealTimeId;

    @NotBlank(message = "Meal time name is required")
    @Size(min = 2, max = 50, message = "Meal time must be between 2 and 50 characters")
    @Enumerated(EnumType.STRING)
    @Column(name = "meal_time", nullable = false, length = 50)
    private Meal_Time mealTime;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Start time is required")
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull(message = "Created at timestamp is required")
    @PastOrPresent(message = "Created at must be in the past or present")
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

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

    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinTable(name = "dish_meal_time",
            joinColumns = @JoinColumn(name = "meal_time_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id")
    )
    private Set<Dish> dishes=new HashSet<>();
}