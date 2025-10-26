//package com.dineswift.auth_service.exception;
//
//import com.dineswift.userservice.exception.CustomAuthenticationException;
//import com.dineswift.userservice.exception.ErrorResponse;
//import com.dineswift.userservice.exception.TokenException;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.security.authorization.AuthorizationDeniedException;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.Collections;
//
//@RestControllerAdvice
//public class AuthExceptionHandler {
//
//
//    @ExceptionHandler(AuthenticationException.class)
//    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                "Authentication Failed", HttpStatus.UNAUTHORIZED, Collections.singletonList(ex.getMessage())
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
//    }
//
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
//        ErrorResponse errorResponse = new ErrorResponse(
//                "Access Denied", HttpStatus.FORBIDDEN, Collections.singletonList(ex.getMessage())
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
//    }
//
//    @ExceptionHandler(AuthorizationDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAuthorizationException(AuthorizationDeniedException ex){
//        ErrorResponse errorResponse = new ErrorResponse(
//                "Authorization Required", HttpStatus.FORBIDDEN, Collections.singletonList(ex.getMessage())
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
//    }
//
//    @ExceptionHandler(CustomAuthenticationException.class)
//    public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(CustomAuthenticationException ex){
//        ErrorResponse errorResponse = new ErrorResponse(
//                "User not properly authenticated", HttpStatus.FORBIDDEN, Collections.singletonList(ex.getMessage())
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
//    }
//
//    @ExceptionHandler(TokenException.class)
//    public ResponseEntity<ErrorResponse> handleTokenException(TokenException ex){
//        ErrorResponse errorResponse = new ErrorResponse(
//                "Token Exception ", HttpStatus.BAD_REQUEST, Collections.singletonList(ex.getMessage())
//        );
//        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
//    }
//}
