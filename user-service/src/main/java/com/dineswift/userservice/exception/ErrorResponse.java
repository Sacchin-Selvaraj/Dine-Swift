package com.dineswift.userservice.exception;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ErrorResponse {

    private String errorMessage;
    private HttpStatus httpStatus;
    private List<String> errors;

    public ErrorResponse(String errorMessage, HttpStatus httpStatus) {
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
    }

    public ErrorResponse(String errorMessage, HttpStatus httpStatus, List<String> errors) {
        this.errorMessage = errorMessage;
        this.httpStatus = httpStatus;
        this.errors = errors;
    }
}
