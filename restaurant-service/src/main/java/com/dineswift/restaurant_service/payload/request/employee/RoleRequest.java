package com.dineswift.restaurant_service.payload.request.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RoleRequest {

    @NotNull(message = "Role IDs are required")
    private List<UUID> roleIds;
}
