package com.dineswift.userservice.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PasswordChangeRequest {

    @NotBlank(message = "Token is required")
    private String token;

    @NotBlank(message = "New Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Password must be at least 8 characters long, contain at least one digit, one uppercase letter, one lowercase letter, one special character, and no spaces"
    )
    private String newPassword;

    @NotBlank(message = "Confirm Password is required")
    private String confirmPassword;
}
