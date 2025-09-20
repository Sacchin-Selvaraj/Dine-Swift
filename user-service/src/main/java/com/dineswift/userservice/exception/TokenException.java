package com.dineswift.userservice.exception;

import javax.naming.AuthenticationException;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
