package com.dineswift.userservice.controller;

import com.dineswift.userservice.model.entites.BookingStatus;
import com.dineswift.userservice.model.request.*;
import com.dineswift.userservice.model.response.*;
import com.dineswift.userservice.service.BookingFilter;
import com.dineswift.userservice.service.CustomPageDto;
import com.dineswift.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/greet")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String greet(){
        return "Hello World !!!";
    }


    @PostMapping("/sign-up")
    public ResponseEntity<Void> signUpUser(@Valid @RequestBody UserRequest userRequest){
        userService.signUpUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginRequest loginRequest){
        log.info("Login request received for email: {}", loginRequest.getEmail());
        UserResponse userResponse=userService.loginUser(loginRequest);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<CustomPageDto<BookingDTO>> getBookings(
            @RequestParam(name = "page") int page,
            @RequestParam(name = "limit") int limit,
            @RequestParam(name = "bookingStatus",required = false) BookingStatus bookingStatus,
            @RequestParam(name = "bookingDate",required = false) LocalDate bookingDate,
            @RequestParam(name = "sortField",defaultValue = "bookingDate" ,required = false) String sortField,
            @RequestParam(name = "sortOrder",defaultValue = "asc" ,required = false) String sortOrder
    ){
        BookingFilter filter = new BookingFilter(page,limit,bookingStatus,bookingDate,sortField,sortOrder);
        CustomPageDto<BookingDTO> bookingDTOS=userService.getBookings(filter);
        return ResponseEntity.ok(bookingDTOS);
    }

    @PatchMapping("/update-users")
    @PreAuthorize(value = "hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> updateUsers(@Valid @RequestBody UserDetailsRequest userDetailsRequest){
        userService.updateDetails(userDetailsRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("User details have been updated Successfully").build());
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> deactivateUser(){
        userService.deactivateUser();
        return ResponseEntity.ok(MessageResponse.builder().message("Account has been deleted Successfully").build());
    }

    @PatchMapping("/update-username")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> updateUserName(@Valid @RequestBody UsernameUpdateRequest usernameRequest){
        userService.updateUsername(usernameRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Username Updated Successfully").build());
    }

    @PostMapping("/update-password")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<MessageResponse> updatePassword(@Valid @RequestBody PasswordUpdateRequest passwordRequest){
        userService.updatePassword(passwordRequest);
        return ResponseEntity.ok(MessageResponse.builder().message("Password Updated Successfully").build());
    }

    @GetMapping("/get-info/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN','ROLE_MANAGER','ROLE_WAITER')")
    public ResponseEntity<GuestInformationResponse> getUserInfo(@PathVariable UUID userId) {
        GuestInformationResponse guestInformationResponse = userService.getUserInfo(userId);
        return ResponseEntity.ok(guestInformationResponse);
    }

    @GetMapping("/get-current-user-info")
    @PreAuthorize("hasAnyRole('ROLE_USER')")
    public ResponseEntity<UserDTO> getCurrentUserInfo() {
        UserDTO userDTO = userService.getCurrentUserInfo();
        return ResponseEntity.ok(userDTO);
    }
}
