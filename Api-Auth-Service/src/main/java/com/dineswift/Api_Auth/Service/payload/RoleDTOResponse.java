package com.dineswift.Api_Auth.Service.payload;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoleDTOResponse {

    @NotBlank(message = "Role name is required")
    private EmployeeRole roleName;
}
