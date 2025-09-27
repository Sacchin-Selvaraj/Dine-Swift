package com.dineswift.restaurant_service.payload.request.employee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmployeeNameRequest {

    @NotBlank(message = "Employee name is required")
    private String employeeName;
}
