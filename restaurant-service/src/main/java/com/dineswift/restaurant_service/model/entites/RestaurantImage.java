package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Lob
    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @NotBlank(message = "Image name is required")
    @Size(min = 1, max = 255, message = "Image name must be between 1 and 255 characters")
    @Column(name = "image_name", nullable = false, length = 255)
    private String imageName;

    @NotBlank(message = "Content type is required")
    @Size(min = 1, max = 100, message = "Content type must be between 1 and 100 characters")
    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @NotNull(message = "File size is required")
    @Min(value = 0, message = "File size must be greater than or equal to 0")
    @Max(value = 104857600, message = "File size cannot exceed 100MB")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", length = 500)
    private String filePath;

    @NotNull(message = "Display order is required")
    @Min(value = 0, message = "Display order must be greater than or equal to 0")
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @UpdateTimestamp
    @NotNull(message = "Uploaded at timestamp is required")
    @Column(name = "uploaded_at", nullable = false)
    private ZonedDateTime uploadedAt;

    @NotNull(message = "Restaurant is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;
}