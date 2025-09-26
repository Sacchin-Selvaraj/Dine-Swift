package com.dineswift.restaurant_service.model.response;

import com.dineswift.restaurant_service.model.entites.Restaurant;
import com.dineswift.restaurant_service.model.entites.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class EmployeeDTO {

    private UUID employeeId;

    @NotBlank(message = "Employee name is required")
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String phoneNumber;

    @NotNull(message = "Active status is required")
    private Boolean employeeIsActive;

    private ZonedDateTime createdAt;

    private UUID lastModifiedBy;

    private ZonedDateTime lastModifiedDate;

    private Restaurant restaurant;

    private Set<Role> roles;

}
