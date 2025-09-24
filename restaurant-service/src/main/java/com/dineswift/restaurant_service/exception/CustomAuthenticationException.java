package com.dineswift.restaurant_service.exception;

public class CustomAuthenticationException extends RuntimeException {
    public CustomAuthenticationException(String message) {
        super(message);
    }
}
