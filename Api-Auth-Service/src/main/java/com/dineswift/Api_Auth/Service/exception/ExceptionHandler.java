package com.dineswift.Api_Auth.Service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;

@RestControllerAdvice
@Slf4j
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, ServerHttpRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("AUTH_ERROR", request.getPath().value(), Collections.singletonList(ex.getMessage()));
        return ResponseEntity.status(401).body(errorResponse);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(TokenException.class)
    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex, ServerHttpRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("TOKEN_ERROR", request.getPath().value(), Collections.singletonList(ex.getMessage()));
        return ResponseEntity.status(403).body(errorResponse);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RemoteApiException.class)
    public ResponseEntity<ErrorResponse> handleRemoteApiException(RemoteApiException ex, ServerHttpRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("REMOTE_API_ERROR", request.getPath().value(), Collections.singletonList(ex.getMessage()));
        return ResponseEntity.status(502).body(errorResponse);
    }
}
