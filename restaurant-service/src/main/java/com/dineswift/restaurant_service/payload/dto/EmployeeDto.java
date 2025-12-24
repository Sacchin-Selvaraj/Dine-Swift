package com.dineswift.restaurant_service.payload.dto;

import com.dineswift.restaurant_service.payload.response.employee.RoleDTOResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class EmployeeDto {

    private UUID employeeId;
    private String employeeName;
    private String email;
    private String phoneNumber;
    private Boolean employeeIsActive;
    private UUID lastModifiedBy;
    private ZonedDateTime lastModifiedDate;
    private Set<RoleDTOResponse> roles;
}
