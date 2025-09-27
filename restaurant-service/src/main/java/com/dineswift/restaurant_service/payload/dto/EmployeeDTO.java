package com.dineswift.restaurant_service.payload.dto;

import com.dineswift.restaurant_service.model.Role;
import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class EmployeeDTO {

    private UUID employeeId;

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Active status is required")
    private Boolean employeeIsActive;

    private Set<RoleDTOResponse> roles;

}
