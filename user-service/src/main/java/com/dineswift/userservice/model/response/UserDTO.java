package com.dineswift.userservice.model.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@RequiredArgsConstructor
public class UserDTO {

    private UUID userId;

    private String username;

    private String email;

    private String password;

    private String gender;

    private String phoneNumber;

    private String firstName;

    private String lastName;

    private Date dob;

    private String address;

    private String area;

    private String city;

    private String district;

    private String state;

    private String country;

    private String pincode;

    private String passwordForgotToken;

    private LocalDateTime tokenExpiryDate;

    private LocalDateTime registrationDate;

    private LocalDateTime systemUpdatedDate;

    private LocalDateTime lastLoginTime;

    private Boolean isActive;

    private Boolean isVerified;

    private CartDTO cart;

}
