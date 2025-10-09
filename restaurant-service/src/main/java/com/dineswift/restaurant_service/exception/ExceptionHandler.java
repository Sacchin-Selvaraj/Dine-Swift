package com.dineswift.restaurant_service.exception;

import jakarta.validation.ConstraintViolationException;
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

        ErrorResponse errorResponse = new ErrorResponse("DataIntegrity Violation ", List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RestaurantException.class)
    public ResponseEntity<ErrorResponse> handleUserException(RestaurantException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Restaurant Details Not valid",List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = getErrors(ex);
        ErrorResponse error = new ErrorResponse( "Method Argument failed", errors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMismatchException(MethodArgumentTypeMismatchException ex){
        List<String> strings= Collections.singletonList(ex.getParameter().toString());
        ErrorResponse error = new ErrorResponse("Method Argument Mismatch ",strings);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex){
        String errorMessage = ex.getLocalizedMessage();
        ErrorResponse error = new ErrorResponse("Illegal Argument ",List.of(errorMessage));
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

    @org.springframework.web.bind.annotation.ExceptionHandler(EmployeeException.class)
    public ResponseEntity<ErrorResponse> handleEmployeeException(EmployeeException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Employee Details Not valid",List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RoleException.class)
    public ResponseEntity<ErrorResponse> handleRoleException(RoleException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Role Details Not valid",List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintException(ConstraintViolationException ex){
        ErrorResponse errorResponse=ErrorResponse.builder().errorMessage("Constraint Violation")
                .errors(List.of(ex.getMessage())).build();
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DishException.class)
    public ResponseEntity<ErrorResponse> handleDishException(DishException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Dish Details Not valid",List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MenuException.class)
    public ResponseEntity<ErrorResponse> handleMenuException(MenuException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Menu Details Not valid",List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(OrderItemException.class)
    public ResponseEntity<ErrorResponse> handleOrderItemException(OrderItemException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Order Item Details Not valid",List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RemoteApiException.class)
    public ResponseEntity<ErrorResponse> handleRemoteApiException(RemoteApiException ex) {
        ErrorResponse errorResponse = new ErrorResponse("Remote API Error", List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
