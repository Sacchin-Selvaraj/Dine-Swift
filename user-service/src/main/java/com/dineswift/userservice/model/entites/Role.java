package com.dineswift.userservice.model.entites;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "role_name", name = "uk_roles_name")
})
@Data
@RequiredArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "role_id", nullable = false, updatable = false)
    private UUID roleId;

    @NotNull(message = "Role name is required")
    @Column(name = "role_name", nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

}
