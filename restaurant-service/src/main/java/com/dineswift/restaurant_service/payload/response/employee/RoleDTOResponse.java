package com.dineswift.restaurant_service.payload.response.employee;

import com.dineswift.restaurant_service.model.RoleName;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RoleDTOResponse {

    private UUID roleId;

    @NotBlank(message = "Role name is required")
    private RoleName roleName;
}
