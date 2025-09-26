package com.dineswift.restaurant_service.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Collections;
import java.util.List;


@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        ErrorResponse errorResponse = new ErrorResponse("DataIntegrity Violation ", HttpStatus.CONFLICT, List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RestaurantException.class)
    public ResponseEntity<ErrorResponse> handleUserException(RestaurantException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Restaurant Details Not valid", HttpStatus.BAD_REQUEST,List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = getErrors(ex);
        ErrorResponse error = new ErrorResponse( "Method Argument failed",HttpStatus.BAD_REQUEST, errors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMismatchException(MethodArgumentTypeMismatchException ex){
        List<String> strings= Collections.singletonList(ex.getParameter().toString());
        ErrorResponse error = new ErrorResponse("Method Argument Mismatch ",HttpStatus.BAD_REQUEST,strings);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex){
        String errorMessage = ex.getLocalizedMessage();
        ErrorResponse error = new ErrorResponse(errorMessage,HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    private static List<String> getErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .toList();
        return errors;
    }
}
