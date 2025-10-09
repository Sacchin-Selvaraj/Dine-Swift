package com.dineswift.restaurant_service.exception;

public class RemoteApiException extends RuntimeException {
    public RemoteApiException(String message) {
        super(message);
    }
}
