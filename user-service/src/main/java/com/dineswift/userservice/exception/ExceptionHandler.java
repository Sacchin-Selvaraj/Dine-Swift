package com.dineswift.userservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.security.auth.login.LoginException;
import java.util.Collections;
import java.util.List;


@RestControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        String errorMessage = ex.getLocalizedMessage();

        if (ex.getMessage().contains("username")) {
            errorMessage = "Username already exists";
        } else if (ex.getMessage().contains("email")) {
            errorMessage = "Email already exists";
        }

        ErrorResponse errorResponse = new ErrorResponse("Data Integrity Exception", request.getRequestURI(), Collections.singletonList(errorMessage));
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse("User Details Not valid",request.getRequestURI() ,List.of(ex.getMessage()));
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,HttpServletRequest request) {
        List<String> errors = getErrors(ex);
        ErrorResponse error = new ErrorResponse( "Method Argument failed",request.getRequestURI(), errors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMismatchException(MethodArgumentTypeMismatchException ex,HttpServletRequest request){
        List<String> errors= Collections.singletonList(ex.getParameter().toString());
        ErrorResponse error = new ErrorResponse("Method Argument Mismatch ",request.getRequestURI(),errors);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,HttpServletRequest request){
        String errorMessage = ex.getLocalizedMessage();
        ErrorResponse error = new ErrorResponse("Illegal Argument Exception",request.getRequestURI(),List.of(errorMessage));
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

    @org.springframework.web.bind.annotation.ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException ex, HttpServletRequest request){
        String errorMessage = ex.getLocalizedMessage();
        ErrorResponse error = new ErrorResponse("Notification Service Exception",request.getRequestURI(), List.of(errorMessage));
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(RemoteApiException.class)
    public ResponseEntity<ErrorResponse> handleRemoteApiException(RemoteApiException ex, HttpServletRequest request){
        String errorMessage = ex.getLocalizedMessage();
        ErrorResponse error = new ErrorResponse("User Service Remote API Exception",request.getRequestURI(), List.of(errorMessage));
        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CartException.class)
    public ResponseEntity<ErrorResponse> handleCartException(CartException ex, HttpServletRequest request){
        String errorMessage = ex.getLocalizedMessage();
        ErrorResponse error = new ErrorResponse("Cart Service Exception",request.getRequestURI(), List.of(errorMessage));
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
