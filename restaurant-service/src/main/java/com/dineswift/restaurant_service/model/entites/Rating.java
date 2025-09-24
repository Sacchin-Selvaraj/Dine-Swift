package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "ratings")
@Data
@RequiredArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id", nullable = false, updatable = false)
    private UUID ratingId;

    @Min(value = 1, message = "Food rating must be at least 1")
    @Max(value = 5, message = "Food rating cannot exceed 5")
    @Column(name = "food_rating")
    private Short foodRating;

    @Min(value = 1, message = "Service rating must be at least 1")
    @Max(value = 5, message = "Service rating cannot exceed 5")
    @Column(name = "service_rating")
    private Short serviceRating;

    @Min(value = 1, message = "Ambiance rating must be at least 1")
    @Max(value = 5, message = "Ambiance rating cannot exceed 5")
    @Column(name = "ambiance_rating")
    private Short ambianceRating;

    @NotNull(message = "Rating created date is required")
    @PastOrPresent(message = "Rating created date must be in the past or present")
    @Column(name = "rating_created_date", nullable = false, updatable = false)
    private ZonedDateTime ratingCreatedDate;

    @PastOrPresent(message = "Rating updated date must be in the past or present")
    @Column(name = "rating_updated_date")
    private ZonedDateTime ratingUpdatedDate;

    @NotNull(message = "Booking Id is Required")
    private UUID bookingId;

    @NotNull(message = "User Id is Required")
    private UUID userId;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}