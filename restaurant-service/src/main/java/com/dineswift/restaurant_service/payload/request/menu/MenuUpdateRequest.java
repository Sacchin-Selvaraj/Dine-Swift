package com.dineswift.restaurant_service.payload.request.menu;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class MenuUpdateRequest {

    private String menuName;
    private String description;
}
