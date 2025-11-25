package com.dineswift.restaurant_service.payload.request.verification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Employee email or PhoneNumber is required")
    private String employeeInput;

    @NotBlank(message = "Type of verification is required")
    private String typeOfVerification;
}
