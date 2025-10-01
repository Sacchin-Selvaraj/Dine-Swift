package com.dineswift.restaurant_service.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoleRequest {

    @NotNull(message = "Role name is required")
    private String roleName;
}
