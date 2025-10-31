package com.dineswift.Api_Auth.Service.payload;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class EmployeeResponse {

    private UUID employeeId;

    private String employeeName;

    private Set<RoleDTOResponse> roles;
}
