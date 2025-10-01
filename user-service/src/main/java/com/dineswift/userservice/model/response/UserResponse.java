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

    private String email;

    private String password;

    private Boolean isActive;

    private Set<Role> roles;

}
