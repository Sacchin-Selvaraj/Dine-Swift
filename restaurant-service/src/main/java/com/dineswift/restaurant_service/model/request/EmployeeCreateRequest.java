package com.dineswift.restaurant_service.model.request;


import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmployeeCreateRequest {


    @NotBlank(message = "Employee name is required")
    @Size(min = 2, max = 255, message = "Employee name must be between 2 and 255 characters")
    private String employeeName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
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
}
