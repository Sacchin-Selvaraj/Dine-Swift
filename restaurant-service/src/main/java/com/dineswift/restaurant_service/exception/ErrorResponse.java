package com.dineswift.restaurant_service.exception;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
@Builder
public class ErrorResponse {

    private String errorMessage;
    private final LocalDateTime timestamp = LocalDateTime.now();
    private List<String> errors;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorResponse(String errorMessage, List<String> errors) {
        this.errorMessage = errorMessage;
        this.errors = errors;
    }
}
