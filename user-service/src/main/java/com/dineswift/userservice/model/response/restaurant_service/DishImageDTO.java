package com.dineswift.userservice.model.response.restaurant_service;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class DishImageDTO {

    private UUID imageId;
    private String publicId;
    private String imageUrl;
    private String secureUrl;
    private String format;
    private String resourceType;
    private Long bytes;
    private Integer width;
    private Integer height;
}
