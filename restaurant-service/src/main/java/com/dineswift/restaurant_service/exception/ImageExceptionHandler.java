package com.dineswift.restaurant_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ImageExceptionHandler {

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<ErrorResponse> handleImageException(ImageException ex) {
        ErrorResponse errorResponse = new ErrorResponse("IMAGE_ERROR", List.of(ex.getMessage()));
        return ResponseEntity.internalServerError().body(errorResponse);
    }
}
