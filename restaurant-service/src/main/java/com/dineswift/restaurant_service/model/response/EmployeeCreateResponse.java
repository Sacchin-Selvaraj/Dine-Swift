package com.dineswift.restaurant_service.model.response;

import com.dineswift.restaurant_service.model.entites.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class EmployeeCreateResponse {

    private UUID employeeId;

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 255, message = "Employee name must be between 2 and 255 characters")
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @Pattern(
            regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{10,20}$",
            message = "Phone number must be valid and between 10-20 digits"
    )
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @NotNull(message = "Active status is required")
    private Boolean employeeIsActive;

    private ZonedDateTime createdAt;

    private UUID lastModifiedBy;

    private ZonedDateTime lastModifiedDate;

    private Set<Role> roles;

}
