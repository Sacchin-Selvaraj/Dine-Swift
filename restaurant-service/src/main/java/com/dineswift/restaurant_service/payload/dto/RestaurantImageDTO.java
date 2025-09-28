package com.dineswift.restaurant_service.payload.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RestaurantImageDTO {

    private UUID imageId;

    private String publicId;

    private String imageUrl;

    private String secureUrl;

    private String format;

    private String resourceType;

    private Long bytes;

    private Integer width;

    private Integer height;

    private LocalDateTime uploadedAt;
}
