package com.dineswift.restaurant_service.model.entites;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "employee", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_employee_email"),
        @UniqueConstraint(columnNames = "phone_number", name = "uk_employee_phone")
})
@Data
@RequiredArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employee_id", nullable = false, updatable = false)
    private UUID employeeId;

    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 255, message = "Employee name must be between 2 and 255 characters")
    @Column(name = "employee_name", nullable = false)
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must contain at least one digit, one lowercase, one uppercase, one special character and no whitespace"
    )
    @Column(name = "password", nullable = false)
    private String password;

    @Pattern(
            regexp = "^[\\+]?[0-9\\s\\-\\(\\)]{10,20}$",
            message = "Phone number must be valid and between 10-20 digits"
    )
    @Column(name = "phone_number", unique = true)
    private String phoneNumber;

    @NotNull(message = "Active status is required")
    @Column(name = "employee_is_active", nullable = false)
    private Boolean employeeIsActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "last_modified_by")
    private UUID lastModifiedBy;

    @UpdateTimestamp
    @Column(name = "last_modified_date", nullable = false)
    private ZonedDateTime lastModifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToMany(cascade = {CascadeType.PERSIST,CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "employee_role",
            joinColumns = @JoinColumn(name = "employee_id",referencedColumnName = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "role_id")
    )
    private Set<Role> roles;

}