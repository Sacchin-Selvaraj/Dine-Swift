package com.dineswift.userservice.model.response;

import com.dineswift.userservice.model.entites.Role;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class UserResponse {

    private UUID userId;

    private String username;

    private Set<RoleDto> roles;

}
