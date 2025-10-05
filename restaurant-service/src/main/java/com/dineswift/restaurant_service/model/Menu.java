package com.dineswift.restaurant_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "menu")
@Data
@RequiredArgsConstructor
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "menu_id", nullable = false, updatable = false)
    private UUID menuId;

    @NotBlank(message = "Menu name is required")
    @Size(min = 1, max = 20, message = "Menu name must be between 1 and 20 characters")
    @Column(name = "menu_name", nullable = false, length = 20)
    private String menuName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private ZonedDateTime createdAt;

    @NotNull(message = "Created by is required")
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

    @PastOrPresent(message = "Last modified date must be in the past or present")
    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private ZonedDateTime lastModifiedDate;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToMany(fetch = FetchType.LAZY,cascade = {CascadeType.PERSIST,CascadeType.MERGE} )
    @JoinTable(name = "menu_dish",
            joinColumns = @JoinColumn(name = "menu_id"),
            inverseJoinColumns = @JoinColumn(name = "dish_id")
    )
    private Set<Dish> dishes=new HashSet<>();
}