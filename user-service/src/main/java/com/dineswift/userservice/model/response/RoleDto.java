package com.dineswift.userservice.model.response;


import com.dineswift.userservice.model.entites.RoleName;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class RoleDto {

    private RoleName roleName;
    private String roleNameString;
}
