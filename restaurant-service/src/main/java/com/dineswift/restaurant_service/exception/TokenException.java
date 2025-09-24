package com.dineswift.restaurant_service.exception;

import javax.naming.AuthenticationException;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
