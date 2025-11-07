package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.request.*;
import com.dineswift.userservice.model.response.BookingDTO;
import com.dineswift.userservice.model.response.GuestInformationResponse;
import com.dineswift.userservice.model.response.UserDTO;
import com.dineswift.userservice.model.response.UserResponse;
import com.dineswift.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/greet")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String greet(){
        return "Hello World";
    }


    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUpUser(@Valid @RequestBody UserRequest userRequest){
        UserResponse userResponse=userService.signUpUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest){
        log.info("Login request received for email: {}", loginRequest.getEmail());
        UserResponse userResponse=userService.loginUser(loginRequest);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/bookings/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Page<BookingDTO>> getBookings(
            @PathVariable UUID userId,
            @RequestParam(name = "page") Integer page,
            @RequestParam(name = "limit") Integer limit,
            @RequestParam(name = "bookingStatus",required = false) BookingStatus bookingStatus,
            @RequestParam(name = "sortField",defaultValue = "bookingDate" ,required = false) String sortField,
            @RequestParam(name = "sortOrder",defaultValue = "asc" ,required = false) String sortOrder
    ){
        Page<BookingDTO> bookingDTOS=userService.getBookings(userId,page,limit,bookingStatus,sortField,sortOrder);
        return ResponseEntity.ok(bookingDTOS);
    }

    @PatchMapping("/update-users/{userId}")
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    public ResponseEntity<UserDTO> updateUsers(@Valid @RequestBody UserDetailsRequest userDetailsRequest, @PathVariable UUID userId){
        UserDTO userDTO=userService.updateDetails(userDetailsRequest,userId);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> deactivateUser(@PathVariable UUID userId){
        userService.deactivateUser(userId);
        return ResponseEntity.ok("Account has been deleted Successfully");
    }

    @PatchMapping("/update-username/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updateUserName(@PathVariable UUID userId, @Valid @RequestBody UsernameUpdateRequest usernameRequest){
        userService.updateUsername(userId,usernameRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Username Updated Successfully");
    }

    @PostMapping("/update-password/{userId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> updatePassword(@PathVariable UUID userId, @Valid @RequestBody PasswordUpdateRequest passwordRequest){
        userService.updatePassword(userId,passwordRequest);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Password Updated Successfully");
    }

    @GetMapping("/get-info/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER')")
    public ResponseEntity<GuestInformationResponse> getUserInfo(@PathVariable UUID userId) {
        GuestInformationResponse guestInformationResponse = userService.getUserInfo(userId);
        return ResponseEntity.ok(guestInformationResponse);
    }
}
