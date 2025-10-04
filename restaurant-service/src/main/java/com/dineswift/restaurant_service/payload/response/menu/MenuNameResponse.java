package com.dineswift.restaurant_service.payload.response.menu;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MenuNameResponse {
    private UUID menuId;
    private String menuName;
    private String description;
}
