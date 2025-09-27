package com.dineswift.restaurant_service.payload.request.employee;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PasswordChangeRequest {

    @NotNull(message = "Old password is required")
    private String oldPassword;

    @NotNull(message = "New password is required")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
             message = "Password must be at least 8 characters long, contain at least one digit, one lowercase letter, one uppercase letter, one special character, and have no whitespace")
    private String newPassword;

    @NotNull(message = "Confirm new password is required")
    private String confirmNewPassword;
}

