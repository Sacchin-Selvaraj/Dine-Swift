package com.dineswift.restaurant_service.model.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "dish_images")
@Data
@RequiredArgsConstructor
public class DishImage {

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

    @Size(max = 50, message = "Content type cannot exceed 50 characters")
    @Column(name = "content_type", length = 50)
    private String contentType;

    @Min(value = 0, message = "File size must be greater than or equal to 0")
    @Max(value = 10485760, message = "File size cannot exceed 10MB")
    @Column(name = "file_size")
    private Integer fileSize;

    @Size(max = 255, message = "File path cannot exceed 255 characters")
    @Column(name = "file_path", length = 255)
    private String filePath;

    @Min(value = 0, message = "Display order must be greater than or equal to 0")
    @Column(name = "display_order")
    private Integer displayOrder;

    @NotNull(message = "Uploaded at timestamp is required")
    @PastOrPresent(message = "Uploaded at must be in the past or present")
    @Column(name = "uploaded_at", nullable = false)
    private ZonedDateTime uploadedAt;

    @NotNull(message = "Dish is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dish_id")
    @JsonIgnore
    private Dish dish;

}