package com.dineswift.userservice.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "User emailId is required")
    private String userEmail;

    @NotBlank(message = "Type of verification is required")
    private String typeOfVerification;
}
