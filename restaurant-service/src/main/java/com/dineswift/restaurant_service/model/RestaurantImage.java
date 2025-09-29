package com.dineswift.restaurant_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant_image")
@Data
@RequiredArgsConstructor
public class RestaurantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id", nullable = false, updatable = false)
    private UUID imageId;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "secure_url", nullable = false, length = 1000)
    private String secureUrl;

    @Column(name = "format")
    private String format;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "width")
    private Integer width;

    @Column(name = "height")
    private Integer height;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Column(name = "uploaded_at")
    @UpdateTimestamp
    private LocalDateTime uploadedAt;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}