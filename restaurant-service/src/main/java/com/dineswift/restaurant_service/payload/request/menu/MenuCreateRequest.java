package com.dineswift.restaurant_service.payload.request.menu;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MenuCreateRequest {

    @NotNull
    @Size(min = 3, max = 100, message = "Category Name should be between 3 and 100 characters")
    private String menuName;

    private String description;

    List<UUID> dishIds;
}
