package com.dineswift.restaurant_service.payload.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoleDTO {

    @NotNull(message = "Role name is required")
    private String roleName;
}
