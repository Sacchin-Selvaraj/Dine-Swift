package com.dineswift.restaurant_service.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class VerifyTokenRequest {

    @NotNull(message = "Token is required")
    @Size(min = 6,max = 8, message = "Min 6 to Max 8 length required")
    private String token;

}
