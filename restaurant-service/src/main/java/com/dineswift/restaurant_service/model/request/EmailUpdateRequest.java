package com.dineswift.restaurant_service.model.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class EmailUpdateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
