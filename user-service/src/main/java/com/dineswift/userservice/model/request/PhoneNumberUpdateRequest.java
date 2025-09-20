package com.dineswift.userservice.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PhoneNumberUpdateRequest {

    @NotBlank(message = "Phone number is required")
    @Size(min = 10, message = "Phone number should contain 10 digits")
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone number must be in Standard format : +91phoneNumber")
    private String phoneNumber;
}
