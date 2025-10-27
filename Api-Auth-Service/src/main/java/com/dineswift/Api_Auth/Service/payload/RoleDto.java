package com.dineswift.Api_Auth.Service.payload;


import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RoleDto {

    private RoleName roleName;
    private String roleNameString;

}
