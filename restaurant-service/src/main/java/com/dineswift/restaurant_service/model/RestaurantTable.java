package com.dineswift.restaurant_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant_table")
@Data
@RequiredArgsConstructor
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "table_id", nullable = false, updatable = false)
    private UUID tableId;

    @NotBlank(message = "Table number is required")
    @Size(min = 1, max = 50, message = "Table number must be between 1 and 50 characters")
    @Column(name = "table_number", nullable = false, length = 50)
    private String tableNumber;

    @Size(max = 1000, message = "Table description cannot exceed 1000 characters")
    @Column(name = "table_description", columnDefinition = "TEXT")
    private String tableDescription;

    @NotNull(message = "Total number of seats is required")
    @Min(value = 1, message = "Total number of seats must be at least 1")
    @Max(value = 100, message = "Total number of seats cannot exceed 100")
    @Column(name = "total_number_of_seats", nullable = false)
    private Integer totalNumberOfSeats;

    @Column(name = "table_shape", length = 20)
    private String tableShape;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false)
    private ZonedDateTime createdDate;

    @NotNull(message = "Created by is required")
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @PastOrPresent(message = "Last modified date must be in the past or present")
    @UpdateTimestamp
    @Column(name = "last_modified_date")
    private ZonedDateTime lastModifiedDate;

    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

    @NotNull(message = "Active status is required")
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    public void deactivate() {
        this.isActive = false;
    }
}
