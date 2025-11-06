package com.dineswift.restaurant_service.payload.request.verification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Employee email is required")
    private String employeeEmail;

    @NotBlank(message = "Type of verification is required")
    private String typeOfVerification;
}
