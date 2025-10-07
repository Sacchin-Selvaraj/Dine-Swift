package com.dineswift.restaurant_service.payload.request.employee;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoleNameRequest {

    private String roleName;
}
