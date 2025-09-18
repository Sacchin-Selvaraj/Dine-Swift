package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.request.LoginRequest;
import com.dineswift.userservice.model.request.UserDetailsRequest;
import com.dineswift.userservice.model.request.UserRequest;
import com.dineswift.userservice.model.response.AuthResponse;
import com.dineswift.userservice.model.response.BookingDTO;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/greet")
    public String greet(){
        return "Hello World";
    }


    @GetMapping("/bookings/{userId}")
    public ResponseEntity<Page<BookingDTO>> getBookings(
            @PathVariable UUID userId,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "bookingStatus",required = false) BookingStatus bookingStatus,
            @RequestParam(name = "sortField",defaultValue = "bookingTime" ,required = false) String sortField,
            @RequestParam(name = "sortOrder",defaultValue = "asc" ,required = false) String sortOrder
    ){
        Page<BookingDTO> bookingDTOS=userService.getBookings(userId,page,limit,bookingStatus,sortField,sortOrder);
        return ResponseEntity.ok(bookingDTOS);
    }

    @PatchMapping("/update-users/{userId}")
    public ResponseEntity<UserDTO> updateUsers(@Valid @RequestBody UserDetailsRequest userDetailsRequest, @PathVariable UUID userId){
        UserDTO userDTO=userService.updateDetails(userDetailsRequest,userId);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/delete/{userId}")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(@PathVariable UUID userId){
        userService.deactivateUser(userId);
        return ResponseEntity.ok("Account has been deleted Successfully");
    }

}
