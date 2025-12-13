package com.dineswift.userservice.exception;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class ErrorResponse {

    private String errorMessage;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String path;
    private List<String> errors;


    public ErrorResponse(String errorMessage, String path, List<String> errors) {
        this.errorMessage = errorMessage;
        this.path = path;
        this.errors = errors;
    }
}
